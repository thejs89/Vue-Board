module.exports = {
  root: true,
  env: {
    node: true
  },
  plugins: [
  ],
  extends: [
    'plugin:vue/essential',
    '@vue/standard'
  ],
  rules: {
    'space-before-function-paren': [0, 'always'],
    'template-curly-spacing': 'off',
    'indent': 'off',
    'no-console': 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off'
  },
  parserOptions: {
    parser: 'babel-eslint'
  }
}

