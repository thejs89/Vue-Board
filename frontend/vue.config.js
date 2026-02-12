const {
  WEB_PORT: port,
  API_PORT: apiPort
} = process.env

const pages = {
  index: {
    entry: 'src/main.js',
    title: '게시판 시스템',
    template: 'public/index.html',
    filename: 'index.html',
    chunks: ['chunk-common', 'chunk-vendors', 'index']
  }
}

const publicPath = process.env.PUBLIC_PATH
const isProduction = process.env.NODE_ENV === 'production'
module.exports = {
  productionSourceMap: !isProduction,
  outputDir: './build/public',
  publicPath,
  pages,
  devServer: {
    port: port || 8081,
    proxy: {
      '/api': {
        target: `http://127.0.0.1:${apiPort || 8080}`,
        changeOrigin: true
      }
    }
  }
}
