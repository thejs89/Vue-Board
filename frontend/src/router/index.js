import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const ROUTE = {
  MAIN: 'index',
  BOARD: {
    LIST: 'board.list',
    VIEW: 'board.view',
    WRITE: 'board.write',
    REPLY: 'board.reply'
  },
  ORGANIZATION: {
    TREE: 'organization.tree'
  },
  PASSWORD: {
    RESET_REQUEST: 'password.resetRequest',
    RESET: 'password.reset'
  }
}

const routes = [
  {
    path: '/',
    name: ROUTE.MAIN,
    redirect: { name: ROUTE.BOARD.LIST }
  },
  {
    path: '/board/list',
    name: ROUTE.BOARD.LIST,
    component: () => import(/* webpackChunkName: "board" */ '@/views/board/BoardList.vue')
  },
  {
    path: '/board/view/:seq',
    name: ROUTE.BOARD.VIEW,
    component: () => import(/* webpackChunkName: "board" */ '@/views/board/BoardView.vue')
  },
  {
    path: '/board/write',
    name: ROUTE.BOARD.WRITE,
    component: () => import(/* webpackChunkName: "board" */ '@/views/board/BoardWrite.vue')
  },
  {
    path: '/board/reply/:parentSeq',
    name: ROUTE.BOARD.REPLY,
    component: () => import(/* webpackChunkName: "board" */ '@/views/board/BoardReply.vue')
  },
  {
    path: '/organization/tree',
    name: ROUTE.ORGANIZATION.TREE,
    component: () => import(/* webpackChunkName: "organization" */ '@/views/organization/OrganizationTree.vue')
  },
  {
    path: '/password/reset-request',
    name: ROUTE.PASSWORD.RESET_REQUEST,
    component: () => import(/* webpackChunkName: "password" */ '@/views/password/PasswordResetRequest.vue')
  },
  {
    path: '/password/reset',
    name: ROUTE.PASSWORD.RESET,
    component: () => import(/* webpackChunkName: "password" */ '@/views/password/PasswordReset.vue')
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export { ROUTE }
export default router
