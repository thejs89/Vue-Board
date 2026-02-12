<template>
  <div class="content-wrapper">
    <div style="margin-bottom:10px">
      <button type="button" class="btn btn-primary" @click="moveWrite">등록</button>
    </div>
    <div class="card">
      <div class="card-body">
        <div class="table-responsive">
          <table class="table table-bordered">
            <thead>
              <tr>
                <th style="width: 80px">#</th>
                <th>제목</th>
                <th style="width: 100px">공개여부</th>
                <th style="width: 100px">작성자</th>
                <th style="width: 100px">작성일</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!items || items.length === 0">
                <td colspan="5" class="text-center" style="padding: 40px;">
                  <i class="fas fa-inbox" style="font-size: 48px; color: #ccc; margin-bottom: 10px;"></i>
                  <p style="margin: 10px 0; color: #999; font-size: 16px;">글이 없습니다</p>
                </td>
              </tr>
              <tr v-for="item in items" :key="item.seq">
                <td>{{ item.rnum }}</td>
                <td>
                  <span v-if="item.depth > 0">
                    <img :src="'/img/dist/level.gif'" :width="(item.depth - 1) * 10" height="16" alt="">
                    RE:
                  </span>
                  <a v-if="!item.deleteYn" href="javascript:void(0)" @click="moveView(item.seq)">{{ item.title }}</a>
                  <a v-else href="javascript:void(0)" @click="alert('삭제된 글')">{{ item.title }}</a>
                </td>
                <td>{{ item.display ? '공개' : '비공개' }}</td>
                <td>{{ item.updId }}</td>
                <td>{{ formatDate(item.updDate) }}</td>
              </tr>
            </tbody>
          </table>
          <el-pagination
            v-bind:rowSize="rowSize"
            v-bind:pageSize="pageSize"
            v-bind:totalCount="totalCount"
            v-bind:currentPage="currentPage"
            v-on:goto="goto"
          >
          </el-pagination>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/utils/api'
import { ROUTE } from '@/router'
import ElPagination from '@/components/list/ElPagination.vue'

export default {
  name: 'BoardList',
  components: { ElPagination },
  data() {
    return {
      pageSize: 10,
      currentPage: 1,
      totalCount: 0,
      rowSize: 10, // 페이지당 행 수 (백엔드에서 받아옴)
      items: []
    }
  },
  mounted() {
    this.initSearch()
  },
  methods: {
    initSearch() {
      this.searchList()
    },
    searchList(page) {
      // 페이지 번호가 없으면 1페이지로 설정
      if (!page) {
        page = 1
      }

      api.get(`/board/list?page=${page}`)
      .then((response) => {
        const pageData = response.data.page
        const list = pageData.contents || []

        // 게시글 목록 저장
        this.items = list
        this.currentPage = pageData.currentPage || page
        this.totalCount = pageData.totalCount || 0
      })
      .catch((error) => {
        console.error('게시글 목록 로드 실패:', error)
        alert('게시글 목록을 불러오는데 실패했습니다.')
      })
    },
    goto(page = 1) {
      this.searchList(page)
    },
    moveWrite() {
      this.$router.push({ name: ROUTE.BOARD.WRITE })
    },
    moveView(seq) {
      this.$router.push({ name: ROUTE.BOARD.VIEW, params: { seq } })
    },
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      return d.toLocaleString('ko-KR')
    }
  }
}
</script>

<style>
</style>
