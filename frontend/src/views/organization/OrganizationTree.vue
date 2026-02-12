<template>
  <div class="content-wrapper">
    <div class="row">
      <!-- 트리 영역 -->
      <div class="col-md-6">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">조직도</h3>
            <div class="card-tools">
              <button type="button" class="btn btn-sm btn-primary" @click="startAddOrganization">
                <i class="fas fa-plus"></i> 조직 추가
              </button>
              <button type="button" class="btn btn-sm btn-success" @click="startMoveOrganization">
                <i class="fas fa-arrows-alt"></i> 조직 이동
              </button>
              <button type="button" class="btn btn-sm btn-secondary" @click="cancelOperation">
                <i class="fas fa-times"></i> 취소
              </button>
            </div>
          </div>
          <div class="card-body" id="jstree" style="min-height: 500px;">
          </div>
        </div>
      </div>
      <!-- 작업 정보 패널 -->
      <div class="col-md-6">
        <!-- 조직 추가 패널 -->
        <div class="card card-primary" v-show="currentMode === 'add'">
          <div class="card-header">
            <h3 class="card-title"><i class="fas fa-plus"></i> 새 조직 추가</h3>
          </div>
          <div class="card-body">
            <div class="form-group">
              <label>조직명</label>
              <input type="text" class="form-control" v-model="newOrgName" placeholder="조직명을 입력하세요">
            </div>
            <div class="form-group">
              <label>부모 조직</label>
              <div class="form-control" style="background-color: #e9ecef; min-height: 40px; padding: 8px;">
                <span v-if="selectedParentId === null" class="text-muted">트리에서 부모 조직을 클릭하세요 (선택하지 않으면 루트에 추가)</span>
                <strong v-else class="text-primary">{{ selectedParentName || '루트 (CEO)' }}</strong>
              </div>
            </div>
            <div class="alert alert-info">
              <i class="fas fa-info-circle"></i> 트리에서 부모 조직을 클릭한 후 조직명을 입력하고 "추가" 버튼을 클릭하세요.
            </div>
            <button type="button" class="btn btn-primary btn-block" @click="addOrganization">
              <i class="fas fa-save"></i> 추가
            </button>
          </div>
        </div>

        <!-- 조직 이동 패널 -->
        <div class="card card-success" v-show="currentMode === 'move'">
          <div class="card-header">
            <h3 class="card-title"><i class="fas fa-arrows-alt"></i> 조직 이동</h3>
          </div>
          <div class="card-body">
            <div class="form-group">
              <label>1단계: 이동할 조직</label>
              <div class="form-control" style="background-color: #e9ecef; min-height: 50px; padding: 8px;">
                <span v-if="selectedOrgId === null" class="text-muted">트리에서 이동할 조직을 클릭하세요</span>
                <strong v-else class="text-primary">✓ {{ selectedOrgName }} (ID: {{ selectedOrgId }})</strong>
                <br v-if="selectedOrgId !== null"><small class="text-success">1단계 완료!</small>
              </div>
            </div>
            <div class="form-group">
              <label>2단계: 새 부모 조직</label>
              <div class="form-control" style="background-color: #e9ecef; min-height: 50px; padding: 8px;">
                <span v-if="newParentId === null" class="text-muted">트리에서 새 부모 조직을 클릭하세요</span>
                <strong v-else class="text-success">✓ {{ newParentName || '루트 (CEO)' }} (ID: {{ newParentId === null ? 'ROOT' : newParentId }})</strong>
                <br v-if="newParentId !== null"><small class="text-success">2단계 완료! 이동 버튼을 클릭하세요.</small>
              </div>
            </div>
            <div class="alert alert-warning">
              <i class="fas fa-exclamation-triangle"></i>
              <strong>사용 방법:</strong><br>
              1. 트리에서 <strong>이동할 조직</strong>을 클릭하세요<br>
              2. 트리에서 <strong>새 부모 조직</strong>을 클릭하세요<br>
              3. "이동" 버튼을 클릭하세요
            </div>
            <button type="button" class="btn btn-success btn-block" @click="executeMoveOrganization">
              <i class="fas fa-check"></i> 이동
            </button>
          </div>
        </div>

        <!-- 기본 안내 패널 -->
        <div class="card" v-show="currentMode === null">
          <div class="card-body">
            <h5><i class="fas fa-info-circle"></i> 사용 안내</h5>
            <hr>
            <p><strong>조직 추가:</strong></p>
            <ol>
              <li>"조직 추가" 버튼 클릭</li>
              <li>트리에서 부모 조직 클릭 (선택사항)</li>
              <li>조직명 입력</li>
              <li>"추가" 버튼 클릭</li>
            </ol>
            <hr>
            <p><strong>조직 이동:</strong></p>
            <ol>
              <li>"조직 이동" 버튼 클릭</li>
              <li>트리에서 이동할 조직 클릭</li>
              <li>트리에서 새 부모 조직 클릭</li>
              <li>"이동" 버튼 클릭</li>
            </ol>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/utils/api'
import 'jstree/dist/themes/default/style.min.css'
import $ from 'jquery'
import 'jstree'

export default {
  name: 'OrganizationTree',
  data() {
    return {
      currentMode: null, // null, 'add', 'move'
      selectedParentId: null,
      selectedParentName: null,
      selectedOrgId: null,
      selectedOrgName: null,
      newParentId: null,
      newParentName: null,
      newOrgName: '',
      $tree: null
    }
  },
  mounted() {
    this.$tree = $('#jstree')
    this.loadTreeData()
  },
  beforeDestroy() {
    if (this.$tree && this.$tree.jstree(true)) {
      this.$tree.jstree('destroy')
    }
  },
  methods: {
    async loadTreeData() {
      try {
        const response = await api.get('/organization/tree')
        // response.data는 { tree: [...] } 형태
        const treeList = response.data.tree || []
        // jstree 형식으로 데이터 변환
        const treeData = []
        for (let i = 0; i < treeList.length; i++) {
          const node = treeList[i]
          treeData.push({
            id: node.id === '#' ? '#' : node.id,
            text: node.text,
            parent: node.parent === '#' ? '#' : node.parent
          })
        }
        this.initJstree(treeData)
      } catch (error) {
        console.error('트리 데이터 로드 실패:', error)
        alert('조직 데이터를 불러오는데 실패했습니다.')
      }
    },
    initJstree(treeData) {
      if (this.$tree.jstree(true)) {
        this.$tree.jstree('destroy')
      }

      this.$tree.off('select_node.jstree')

      this.$tree.jstree({
        core: {
          multiple: false,
          check_callback: true,
          data: treeData
        }
      })

      this.$tree.on('select_node.jstree', (e, data) => {
        this.onNodeSelected(data)
      })
    },
    onNodeSelected(data) {
      const node = data.node
      const orgName = node.text
      const orgId = node.id === '#' ? null : parseInt(node.id)

      if (this.currentMode === 'add') {
        this.selectedParentId = orgId
        this.selectedParentName = orgName
      } else if (this.currentMode === 'move') {
        if (this.selectedOrgId === null) {
          if (orgId === null) {
            alert('루트 노드(CEO)는 이동할 수 없습니다.')
            this.clearTreeSelection()
            return
          }
          this.selectedOrgId = orgId
          this.selectedOrgName = orgName
          this.newParentId = null
          this.newParentName = null
        } else {
          if (orgId === this.selectedOrgId) {
            alert('자기 자신을 부모로 선택할 수 없습니다.')
            this.clearTreeSelection()
            return
          }
          this.newParentId = orgId
          this.newParentName = orgName
        }
      }
    },
    startAddOrganization() {
      this.resetState()
      this.currentMode = 'add'
      this.newOrgName = ''
      this.selectedParentId = null
      this.selectedParentName = null
      this.clearTreeSelection()
    },
    startMoveOrganization() {
      this.resetState()
      this.currentMode = 'move'
      this.selectedOrgId = null
      this.selectedOrgName = null
      this.newParentId = null
      this.newParentName = null
      this.clearTreeSelection()
    },
    cancelOperation() {
      this.resetState()
      this.clearTreeSelection()
    },
    resetState() {
      this.currentMode = null
      this.selectedParentId = null
      this.selectedParentName = null
      this.selectedOrgId = null
      this.selectedOrgName = null
      this.newParentId = null
      this.newParentName = null
      this.newOrgName = ''
    },
    clearTreeSelection() {
      if (this.$tree && this.$tree.jstree(true)) {
        this.$tree.jstree('deselect_all')
      }
    },
    async addOrganization() {
      if (!this.newOrgName || !this.newOrgName.trim()) {
        alert('조직명을 입력해주세요.')
        return
      }

      try {
        const params = new URLSearchParams()
        params.append('orgName', this.newOrgName)
        if (this.selectedParentId !== null) {
          params.append('parentOrgId', this.selectedParentId)
        }

        const response = await api.post('/organization/add', params, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })

        if (response.data.success) {
          alert('조직이 추가되었습니다.')
          this.resetState()
          this.loadTreeData()
        } else {
          alert('조직 추가 실패: ' + (response.data.message || '알 수 없는 오류'))
        }
      } catch (error) {
        console.error('조직 추가 실패:', error)
        alert('조직 추가 중 오류가 발생했습니다.')
      }
    },
    async executeMoveOrganization() {
      if (this.selectedOrgId === null) {
        alert('1단계: 이동할 조직을 선택해주세요.\n\n트리에서 이동할 조직을 클릭하세요.')
        return
      }

      if (this.newParentId === null) {
        alert('2단계: 새 부모 조직을 선택해주세요.\n\n트리에서 새 부모 조직을 클릭하세요.')
        return
      }

      try {
        const params = new URLSearchParams()
        params.append('orgId', this.selectedOrgId)
        params.append('newParentId', this.newParentId === null ? '' : this.newParentId)

        const response = await api.post('/organization/move', params, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })

        if (response.data.success) {
          alert('조직이 이동되었습니다.')
          this.resetState()
          this.loadTreeData()
        } else {
          alert('조직 이동 실패: ' + (response.data.message || '알 수 없는 오류'))
        }
      } catch (error) {
        console.error('조직 이동 실패:', error)
        alert('조직 이동 중 오류가 발생했습니다.')
      }
    }
  }
}
</script>

<style>
</style>
