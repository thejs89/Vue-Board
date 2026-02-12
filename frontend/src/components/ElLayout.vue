<template>
  <div class="wrapper">
    <!--Header 영역-->
    <el-header/>

    <!-- Sidebar -->
    <el-snb/>

    <!-- Content Wrapper -->
    <div class="content-wrapper">
      <!-- Content Header -->
      <div class="content-header">
        <div class="container-fluid">
          <div class="row mb-2">
            <div class="col-sm-6">
              <h1 class="m-0">{{ pageTitle }}</h1>
            </div>
            <div class="col-sm-6">
              <ol class="breadcrumb float-sm-right">
                <li class="breadcrumb-item"><a href="#">Home</a></li>
                <li class="breadcrumb-item active">{{ pageTitle }}</li>
              </ol>
            </div>
          </div>
        </div>
      </div>
      <!-- Main content -->
      <section class="content">
        <div class="container-fluid">
          <slot/>
        </div>
      </section>
      <!-- Footer -->
      <el-footer/>
    </div>
  </div>
</template>

<script>
import ElHeader from '@/components/ElHeader.vue'
import ElSnb from '@/components/ElSnb.vue'
import ElFooter from '@/components/ElFooter.vue'

export default {
  components: { ElHeader, ElSnb, ElFooter },
  computed: {
    pageTitle() {
      const routeName = this.$route.name
      if (routeName && routeName.includes('board')) {
        return '공지사항'
      } else if (routeName && routeName.includes('organization')) {
        return '조직도'
      } else if (routeName && routeName.includes('password')) {
        return '비밀번호 재설정'
      }
      return '게시판'
    }
  }
}
</script>

<style>
</style>
