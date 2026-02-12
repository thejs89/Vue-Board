<template>
  <div id="app" class="hold-transition sidebar-mini">
    <el-layout>
      <router-view/>
    </el-layout>
  </div>
</template>

<script>
import ElLayout from '@/components/ElLayout.vue'

export default {
  components: { ElLayout },
  data() {
    return {
    }
  },
  methods: {
  }
}
</script>
