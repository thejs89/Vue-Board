module.exports = {
  presets: [
    ['@vue/app', {
      polyfills: [
        'es.promise',
        'es.symbol'
      ]
    }]
  ],
  env: {
    production: {
      plugins: ['transform-remove-console']
    }
  }
}
