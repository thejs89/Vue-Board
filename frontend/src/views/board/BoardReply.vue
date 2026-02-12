<template>
  <div class="content-wrapper">
    <div class="card card-primary">
      <div class="card-header">
        <h3 class="card-title">게시판 답글 등록</h3>
      </div>
      <div class="card-body">
        <div class="form-group">
          <label>제목 (원글)</label>
          <input type="text" class="form-control" :value="parentBoard.title" disabled>
        </div>
        <div class="form-group">
          <label>내용 (원글)</label>
          <textarea class="form-control" rows="3" :value="parentBoard.content" disabled></textarea>
        </div>
        <div class="form-check">
          <input type="checkbox" class="form-check-input" :checked="parentBoard.display" disabled>
          <label class="form-check-label">공개 (원글)</label>
        </div>
      </div>
      <form id="replyForm" @submit.prevent="save">
        <div class="card-body">
          <div class="form-group">
            <label>제목</label>
            <input type="hidden" name="parentSeq" :value="parentSeq">
            <input type="text" class="form-control" v-model="form.title" placeholder="Enter title" required>
          </div>
          <div class="form-group">
            <label>내용</label>
            <textarea class="form-control" rows="3" v-model="form.content" placeholder="Enter ..." required></textarea>
          </div>
          <div class="form-check">
            <input type="checkbox" class="form-check-input" v-model="form.display" id="replyDisplay">
            <label class="form-check-label" for="replyDisplay">공개</label>
          </div>
          <div class="form-group">
            <label>첨부파일</label>
            <div class="input-group">
              <div class="custom-file">
                <input type="file" id="replyFileInput" class="custom-file-input" multiple @change="handleFileChange" ref="fileInput">
                <label class="custom-file-label" for="replyFileInput">{{ fileLabel }}</label>
              </div>
              <div class="input-group-append">
                <button type="button" class="btn btn-outline-secondary" @click="selectFile">
                  <i class="fas fa-folder-open"></i> 선택
                </button>
              </div>
            </div>
            <div class="mt-2" v-if="fileList.length > 0">
              <div class="card card-outline card-secondary">
                <div class="card-header">
                  <h3 class="card-title">
                    <i class="fas fa-paperclip"></i> 선택된 파일 목록
                  </h3>
                  <div class="card-tools">
                    <button type="button" class="btn btn-tool btn-sm" @click="deleteSelectedFiles">
                      <i class="fas fa-trash"></i> 삭제
                    </button>
                  </div>
                </div>
                <div class="card-body p-0">
                  <select id="replyFileSelect" class="form-control" size="8" multiple style="border: none; min-height: 200px;" ref="fileSelect">
                    <option v-for="(file) in fileList" :key="file.key" :value="file.key">
                      {{ getFileDisplayText(file) }}
                    </option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="card-footer">
          <button type="submit" class="btn btn-primary float-right">저장</button>
          <button type="button" class="btn btn-primary" @click="moveList">목록</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import api from '@/utils/api'
import { ROUTE } from '@/router'

export default {
  name: 'BoardReply',
  data() {
    return {
      parentSeq: null,
      parentBoard: {},
      form: {
        title: '',
        content: '',
        display: false
      },
      fileList: [],
      fileLabel: '파일을 선택하세요'
    }
  },
  mounted() {
    this.parentSeq = this.$route.params.parentSeq
    this.loadParentBoard()
  },
  methods: {
    async loadParentBoard() {
      try {
        const response = await api.get(`/board/${this.parentSeq}`)
        this.parentBoard = response.data.board
      } catch (error) {
        console.error('원글 로드 실패:', error)
        alert('원글을 불러오는데 실패했습니다.')
        this.$router.push({ name: ROUTE.BOARD.LIST })
      }
    },
    selectFile() {
      this.$refs.fileInput.click()
    },
    handleFileChange(event) {
      const files = Array.from(event.target.files)
      const existingKeys = new Set(this.fileList.map(f => f.key))

      // 선택한 파일들을 목록에 추가 (REG 모드)
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        const fileKey = `new::${file.name}::${Date.now()}`

        // 중복 제거
        if (!existingKeys.has(fileKey)) {
          this.fileList.push({
            key: fileKey,
            mode: 'REG',
            name: file.name,
            size: file.size,
            type: file.type,
            file: file
          })
        }
      }
      this.updateFileLabel()
      this.$refs.fileInput.value = ''
    },
    deleteSelectedFiles() {
      const select = this.$refs.fileSelect
      // 선택된 파일 key 가져오기
      const selectedKeys = []
      for (let i = 0; i < select.selectedOptions.length; i++) {
        selectedKeys.push(select.selectedOptions[i].value)
      }

      // 선택된 파일 처리
      const newFileList = []
      for (let i = 0; i < this.fileList.length; i++) {
        const file = this.fileList[i]
        const isSelected = selectedKeys.includes(file.key)

        if (!isSelected) {
          // 선택되지 않은 파일은 그대로 유지
          newFileList.push(file)
        } else {
          // REG 모드인 경우 완전히 제거
          if (file.mode === 'REG') {
            // (아무것도 추가하지 않음)
          }
        }
      }
      this.fileList = newFileList
      this.updateFileLabel()
    },
    getFileDisplayText(file) {
      if (file.mode === 'REG') {
        return `${file.name} (새파일)`
      } else if (file.mode === 'DEL') {
        return `${file.name} (삭제)`
      } else {
        return file.name
      }
    },
    updateFileLabel() {
      this.fileLabel = this.fileList.length > 0 ? `${this.fileList.length}개의 파일이 선택되었습니다` : '파일을 선택하세요'
    },
    async save() {
      try {
        const formData = new FormData()

        // REG 모드인 파일만 업로드
        const uploadFiles = []
        for (let i = 0; i < this.fileList.length; i++) {
          const fileItem = this.fileList[i]
          if (fileItem.mode === 'REG') {
            uploadFiles.push(fileItem)
            formData.append('file', fileItem.file)
          }
        }

        // 파일 정보 생성 (file, key, mode 제외)
        const fileInfo = []
        for (let i = 0; i < uploadFiles.length; i++) {
          const { file, key, mode, ...rest } = uploadFiles[i]
          fileInfo.push(rest)
        }
        formData.append('fileInfo', JSON.stringify(fileInfo))

        // 기본 파일 정보
        formData.append('baseFileInfo', JSON.stringify({ id: 'BOARD' }))

        // 게시글 정보
        formData.append('parentSeq', this.parentSeq)
        formData.append('title', this.form.title)
        formData.append('content', this.form.content)
        formData.append('display', this.form.display)

        const response = await api.post('/board/reply', formData)

        if (response.data.success) {
          alert(response.data.message || '답글이 등록되었습니다.')
          this.$router.push({ name: ROUTE.BOARD.LIST })
        }
      } catch (error) {
        console.error('답글 저장 실패:', error)
        alert('답글 저장에 실패했습니다.')
      }
    },
    moveList() {
      this.$router.push({ name: ROUTE.BOARD.LIST })
    }
  }
}
</script>

<style>
</style>
