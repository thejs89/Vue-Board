<template>
  <div class="content-wrapper">
    <div class="row">
      <div class="col-md-6 offset-md-3">
        <div class="card card-primary">
          <div class="card-header">
            <h3 class="card-title">비밀번호 재설정 요청</h3>
          </div>

          <form id="resetRequestForm">
            <div class="card-body">
              <div v-if="error" class="alert alert-danger" role="alert">
                {{ error }}
              </div>

              <div class="form-group">
                <label for="email">이메일 주소</label>
                <input type="email"
                       class="form-control"
                       id="email"
                       v-model="email"
                       placeholder="가입하신 이메일 주소를 입력하세요"
                       required>
                <small class="form-text text-muted">
                  비밀번호 재설정 링크가 이메일로 발송됩니다.
                </small>
              </div>
            </div>

            <div class="card-footer">
              <button type="button" class="btn btn-primary" @click="sendResetMail">메일 발송</button>
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

export default {
  name: 'PasswordResetRequest',
  data() {
    return {
      email: '',
      error: null
    }
  },
  methods: {
    async sendResetMail() {
      if (!this.email || !this.email.trim()) {
        alert('이메일 주소를 입력해주세요.')
        return
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(this.email)) {
        alert('올바른 이메일 형식이 아닙니다.')
        return
      }

      try {
        const params = new URLSearchParams()
        params.append('email', this.email)

        const response = await api.post('/password/reset-request', params, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })

        if (response.data.success) {
          alert(response.data.message)
          this.email = ''
          this.error = null
        } else {
          this.error = response.data.message || '메일 발송에 실패했습니다.'
        }
      } catch (error) {
        console.error('메일 발송 실패:', error)
        this.error = error.response?.data?.message || '오류가 발생했습니다.'
      }
    },
    cancel() {
      this.email = ''
      this.error = null
    }
  }
}
</script>

<style>
</style>
