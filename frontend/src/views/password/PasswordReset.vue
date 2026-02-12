<template>
  <div class="content-wrapper">
    <div class="row">
      <div class="col-md-6 offset-md-3">
        <div class="card card-primary">
          <div class="card-header">
            <h3 class="card-title">새 비밀번호 설정</h3>
          </div>
          
          <form id="resetForm">
            <div class="card-body">
              <div class="form-group">
                <label>이메일</label>
                <input type="email" 
                       class="form-control" 
                       :value="email" 
                       disabled>
              </div>
              
              <div class="form-group">
                <label for="newPassword">새 비밀번호</label>
                <input type="password" 
                       class="form-control" 
                       id="newPassword" 
                       v-model="newPassword"
                       placeholder="새 비밀번호를 입력하세요"
                       required
                       minlength="8">
                <small class="form-text text-muted">
                  비밀번호는 최소 8자 이상이어야 합니다.
                </small>
              </div>
              
              <div class="form-group">
                <label for="confirmPassword">비밀번호 확인</label>
                <input type="password" 
                       class="form-control" 
                       id="confirmPassword" 
                       v-model="confirmPassword"
                       placeholder="비밀번호를 다시 입력하세요"
                       required>
              </div>
              
              <div v-if="errorMessage" class="alert alert-danger" role="alert">
                {{ errorMessage }}
              </div>
              <div v-if="successMessage" class="alert alert-success" role="alert">
                {{ successMessage }}
              </div>
            </div>
            
            <div class="card-footer">
              <button type="button" class="btn btn-primary" @click="resetPassword">비밀번호 변경</button>
              <button type="button" class="btn btn-secondary" @click="cancel">취소</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/utils/api'
import { ROUTE } from '@/router'

export default {
  name: 'PasswordReset',
  data() {
    return {
      token: '',
      email: '',
      newPassword: '',
      confirmPassword: '',
      errorMessage: '',
      successMessage: ''
    }
  },
  mounted() {
    this.token = this.$route.query.token || ''
    if (!this.token) {
      alert('유효하지 않은 링크입니다.')
      this.$router.push({ name: ROUTE.PASSWORD.RESET_REQUEST })
      return
    }
    this.verifyToken()
  },
  methods: {
    async verifyToken() {
      try {
        const response = await api.get('/password/verify', {
          params: { token: this.token }
        })
        
        if (response.data.success) {
          this.email = response.data.email || ''
        } else {
          alert('유효하지 않은 토큰입니다.')
          this.$router.push({ name: ROUTE.PASSWORD.RESET_REQUEST })
        }
      } catch (error) {
        console.error('토큰 검증 실패:', error)
        alert('토큰 검증에 실패했습니다.')
        this.$router.push({ name: ROUTE.PASSWORD.RESET_REQUEST })
      }
    },
    async resetPassword() {
      this.errorMessage = ''
      this.successMessage = ''
      
      if (!this.newPassword || !this.newPassword.trim()) {
        this.errorMessage = '새 비밀번호를 입력해주세요.'
        return
      }
      
      if (this.newPassword.length < 8) {
        this.errorMessage = '비밀번호는 최소 8자 이상이어야 합니다.'
        return
      }
      
      if (this.newPassword !== this.confirmPassword) {
        this.errorMessage = '비밀번호가 일치하지 않습니다.'
        return
      }
      
      try {
        const params = new URLSearchParams()
        params.append('token', this.token)
        params.append('newPassword', this.newPassword)
        params.append('confirmPassword', this.confirmPassword)
        
        const response = await api.post('/password/reset', params, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })
        
        if (response.data.success) {
          this.successMessage = response.data.message
          setTimeout(() => {
            this.$router.push({ name: ROUTE.PASSWORD.RESET_REQUEST })
          }, 2000)
        } else {
          this.errorMessage = response.data.message || '비밀번호 변경에 실패했습니다.'
        }
      } catch (error) {
        console.error('비밀번호 재설정 실패:', error)
        this.errorMessage = error.response?.data?.message || '오류가 발생했습니다.'
      }
    },
    cancel() {
      this.$router.push({ name: ROUTE.PASSWORD.RESET_REQUEST })
    }
  }
}
</script>

<style>
</style>

