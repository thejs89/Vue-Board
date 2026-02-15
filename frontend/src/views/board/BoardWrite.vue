<template>
  <div class="content-wrapper">
    <div class="card card-primary">
      <div class="card-header">
        <h3 class="card-title">{{ isEditMode ? '게시판 수정' : '게시판 등록' }}</h3>
      </div>
      <form id="saveForm" @submit.prevent="save">
        <div class="card-body">
          <div class="form-group">
            <label>제목</label>
            <input type="text" class="form-control" v-model="form.title" placeholder="Enter title" required>
          </div>
          <div class="form-group">
            <label>내용</label>
            <textarea class="form-control" rows="3" v-model="form.content" placeholder="Enter ..." required></textarea>
          </div>
          <div class="form-check">
            <input type="checkbox" class="form-check-input" v-model="form.display" id="display">
            <label class="form-check-label" for="display">공개</label>
          </div>
          <div class="form-group">
            <label>첨부파일</label>
            <div class="input-group">
              <div class="custom-file">
                <input type="file" id="fileInput" class="custom-file-input" multiple @change="handleFileChange" ref="fileInput">
                <label class="custom-file-label" for="fileInput">{{ fileLabel }}</label>
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
                  <select id="mySelect" class="form-control" size="8" multiple style="border: none; min-height: 200px;" ref="fileSelect">
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
          <button type="submit" class="btn btn-primary float-right">{{ isEditMode ? '수정' : '저장' }}</button>
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
  name: 'BoardWrite',
  data() {
    return {
      seq: null,
      isEditMode: false,
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
    this.seq = this.$route.params.seq
    this.isEditMode = !!this.seq
    if (this.isEditMode) {
      this.loadBoard()
    }
  },
  methods: {
    async loadBoard() {
      try {
        const response = await api.get(`/board/${this.seq}`)
        const board = response.data.board
        const files = response.data.fileList || []
        
        this.form.title = board.title
        this.form.content = board.content
        this.form.display = board.display
        
        // 기존 파일 목록을 UPD 모드로 추가
        for (let i = 0; i < files.length; i++) {
          const file = files[i]
          this.fileList.push({
            key: file.fileSeq.toString(),
            mode: 'UPD',
            name: file.fileName,
            size: file.fileSize,
            type: '',
            fileSeq: file.fileSeq
          })
        }
        this.updateFileLabel()
      } catch (error) {
        console.error('게시글 로드 실패:', error)
        alert('게시글을 불러오는데 실패했습니다.')
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
          // 선택된 파일 처리
          if (file.mode === 'REG') {
            // REG 모드인 경우 완전히 제거
            // (아무것도 추가하지 않음)
          } else {
            // UPD 모드인 경우 DEL 모드로 변경
            newFileList.push({
              ...file,
              mode: 'DEL'
            })
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

        // DEL 모드인 파일의 key 배열
        const removeFiles = []
        for (let i = 0; i < this.fileList.length; i++) {
          if (this.fileList[i].mode === 'DEL') {
            removeFiles.push(this.fileList[i].key)
          }
        }
        formData.append('removeFiles', JSON.stringify({ key: removeFiles }))

        // 기본 파일 정보
        formData.append('baseFileInfo', JSON.stringify({ id: 'BOARD' }))

        // 게시글 정보
        formData.append('title', this.form.title)
        formData.append('content', this.form.content)
        formData.append('display', this.form.display)

        // FormData를 사용하면 axios가 자동으로 multipart/form-data로 설정함
        let response
        if (this.isEditMode) {
          response = await api.put(`/board/${this.seq}`, formData)
        } else {
          response = await api.post('/board', formData)
        }

        if (response.data.success) {
          alert(response.data.message || (this.isEditMode ? '게시글이 수정되었습니다.' : '게시글이 등록되었습니다.'))
          this.$router.push({ name: ROUTE.BOARD.LIST })
        }
      } catch (error) {
        console.error('게시글 저장 실패:', error)
        alert('게시글 저장에 실패했습니다.')
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
