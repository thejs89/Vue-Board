<template>
  <div class="pagination">
    <template v-if="totalCount > 0">
      <a href="#" @click.prevent="goto({ to: 'prev' })">&laquo;</a>
    </template>
    <template v-for="page in pageList">
        <template v-if="currentPage === page">
          <a :key="page" href="#" @click.prevent>{{ page }}</a>
        </template>
        <template v-else>
          <a :key="page" href="#" @click.prevent="goto({ page })" >{{ page }}</a>
        </template>
    </template>
    <template v-if="totalCount > 0">
      <a href="#" @click.prevent="goto({ to: 'next' })">&raquo;</a>
    </template>
  </div>
</template>

<script>
export default {
  props: {
    rowSize: { type: Number, default: () => 10 }, // 페이지당 행 수 (displayRow)
    pageSize: { type: Number, default: () => 10 }, // 페이지네이션에 표시할 페이지 수 (displayPage)
    totalCount: { type: Number, default: () => 0 },
    currentPage: { type: Number, default: () => 1 }
  },
  computed: {
    // 전체 페이지 수 계산 (총 개수 / 페이지당 행 수)
    totalPage() {
      return Math.ceil(this.totalCount / this.rowSize)
    },
    pageList() {
      const { pageSize, currentPage } = this

      // 현재 페이지가 속한 범위 계산 (예: 1-10, 11-20, 21-30...)
      const currentRange = parseInt(((currentPage - 1) / pageSize) + 1, 10)

      // 표시할 페이지 번호의 시작과 끝 계산
      const start = (currentRange - 1) * pageSize + 1
      const end = Math.min(start + pageSize - 1, this.totalPage)

      // 페이지 번호 배열 생성
      const pageList = []
      for (let i = start; i <= end; i++) {
        pageList.push(i)
      }

      return pageList
    }
  },
  methods: {
    goto({ page = 0, to = '' }) {
      const goPage = this.getGoPage({ page, to })
      if (goPage > 0) {
        this.$emit('goto', goPage)
      }
    },
    getGoPage({ page = 0, to = '' }) {
      const totalPage = this.totalPage

      // 특정 페이지 번호로 이동하는 경우
      if (page > 0) {
        // 현재 페이지와 같거나 유효하지 않은 페이지면 이동하지 않음
        if (this.currentPage === page || page < 1 || page > totalPage) {
          return 0
        }
        return page
      }

      // 이전/다음 범위로 이동하는 경우
      const currentRange = Math.floor((this.currentPage - 1) / this.pageSize)

      if (to === 'prev') {
        // 이전 범위의 첫 페이지
        const prevPage = Math.max(1, currentRange * this.pageSize)
        if (this.currentPage === prevPage || prevPage < 1 || prevPage > totalPage) {
          return 0
        }
        return prevPage
      } else if (to === 'next') {
        // 다음 범위의 첫 페이지
        const nextPage = Math.min(totalPage, (currentRange + 1) * this.pageSize + 1)
        if (this.currentPage === nextPage || nextPage < 1 || nextPage > totalPage) {
          return 0
        }
        return nextPage
      }

      return 0
    }
  }
}
</script>

<style scoped>
.pagination {
  display: inline-block;
}
.pagination a {
  color: black;
  float: left;
  padding: 8px 16px;
  text-decoration: none;
}
</style>
