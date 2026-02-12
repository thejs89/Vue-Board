<template>
  <aside class="main-sidebar sidebar-dark-primary elevation-4">
    <!-- Brand Logo -->
    <router-link to="/board/list" class="brand-link">
      <span class="brand-text font-weight-light">게시판</span>
    </router-link>

    <!-- Sidebar -->
    <div class="sidebar">
      <!-- Sidebar Menu -->
      <nav class="mt-2">
        <ul class="nav nav-pills nav-sidebar flex-column" data-widget="treeview" role="menu" data-accordion="false">
          <li class="nav-item">
            <router-link to="/board/list" class="nav-link">
              <i class="nav-icon fas fa-list"></i>
              <p>게시판 목록</p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/organization/tree" class="nav-link">
              <i class="nav-icon fas fa-sitemap"></i>
              <p>조직도</p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/password/reset-request" class="nav-link">
              <i class="nav-icon fas fa-key"></i>
              <p>비밀번호 재설정</p>
            </router-link>
          </li>
        </ul>
      </nav>
    </div>
  </aside>
</template>

<script>
export default {
}
</script>

<style>
</style>

