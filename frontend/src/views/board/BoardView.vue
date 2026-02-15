<template>
  <div class="content-wrapper">
    <div class="card card-primary">
      <div class="card-header">
        <h3 class="card-title">게시판 등록</h3>
      </div>
      <div class="card-body">
        <div class="form-group">
          <label>제목</label>
          <input type="text" class="form-control" :value="board.title" disabled>
        </div>
        <div class="form-group">
          <label>내용</label>
          <textarea class="form-control" rows="3" :value="board.content" disabled></textarea>
        </div>
        <div class="form-check">
          <input type="checkbox" class="form-check-input" :checked="board.display" disabled>
          <label class="form-check-label">공개</label>
        </div>
        <div class="form-group" v-if="fileList && fileList.length > 0">
          <label>첨부파일</label>
          <div class="table-responsive">
            <table class="table table-bordered table-hover">
              <thead>
                <tr>
                  <th style="width: 5%">번호</th>
                  <th style="width: 50%">파일명</th>
                  <th style="width: 15%">파일크기</th>
                  <th style="width: 20%">등록일</th>
                  <th style="width: 10%">다운로드</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(file, index) in fileList" :key="file.fileSeq">
                  <td>{{ index + 1 }}</td>
                  <td>{{ file.fileName }}</td>
                  <td>{{ formatFileSize(file.fileSize) }} KB</td>
                  <td>{{ formatDate(file.regDate) }}</td>
                  <td>
                    <button class="btn btn-sm btn-primary" @click="downloadFile(file.fileSeq)">다운로드</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div class="card-footer">
        <button type="button" class="btn btn-primary float-right" style="margin-left: 10px;" @click="deleteBoard">삭제</button>
        <button type="button" class="btn btn-primary float-right" style="margin-left: 10px;" @click="moveEdit">수정</button>
        <button type="button" class="btn btn-primary float-right" @click="moveReply">답글</button>
        <button type="button" class="btn btn-primary" @click="moveList">목록</button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/utils/api'
import { ROUTE } from '@/router'

export default {
  name: 'BoardView',
  data() {
    return {
      board: {},
      fileList: []
    }
  },
  mounted() {
    this.loadBoard()
  },
  methods: {
    async loadBoard() {
      try {
        const seq = this.$route.params.seq
        const response = await api.get(`/board/${seq}`)
        this.board = response.data.board
        this.fileList = response.data.fileList || []
      } catch (error) {
        console.error('게시글 로드 실패:', error)
        alert('게시글을 불러오는데 실패했습니다.')
        this.$router.push({ name: ROUTE.BOARD.LIST })
      }
    },
    async downloadFile(fileSeq) {
      try {
        const response = await api.get('/board/file/download', {
          params: { fileSeq, boardSeq: this.board.seq },
          responseType: 'blob'
        })
        const fileInfo = this.fileList.find(f => f.fileSeq === fileSeq)
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', fileInfo.fileName)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('파일 다운로드 실패:', error)
        alert('파일 다운로드에 실패했습니다.')
      }
    },
    async deleteBoard() {
      if (!confirm('정말 삭제하시겠습니까?')) return
      try {
        const response = await api.delete(`/board/${this.board.seq}`)
        if (response.data.success) {
          alert(response.data.message || '게시글이 삭제되었습니다.')
          this.$router.push({ name: ROUTE.BOARD.LIST })
        }
      } catch (error) {
        console.error('게시글 삭제 실패:', error)
        alert('게시글 삭제에 실패했습니다.')
      }
    },
    moveEdit() {
      // 답글인지 확인 (depth > 0이면 답글)
      if (this.board.depth > 0) {
        // 답글 수정
        this.$router.push({ name: ROUTE.BOARD.REPLY, params: { seq: this.board.seq, parentSeq: this.board.groupId } })
      } else {
        // 일반 게시글 수정
        this.$router.push({ name: ROUTE.BOARD.WRITE, params: { seq: this.board.seq } })
      }
    },
    moveReply() {
      this.$router.push({ name: ROUTE.BOARD.REPLY, params: { parentSeq: this.board.seq } })
    },
    moveList() {
      this.$router.push({ name: ROUTE.BOARD.LIST })
    },
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      return d.toLocaleString('ko-KR')
    },
    formatFileSize(size) {
      if (!size) return '0'
      return (size / 1024).toFixed(1)
    }
  }
}
</script>

<style>
</style>
