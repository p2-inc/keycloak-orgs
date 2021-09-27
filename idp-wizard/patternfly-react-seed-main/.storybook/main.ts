// https://storybook.js.org/blog/storybook-for-webpack-5/
module.exports = {
  // https://gist.github.com/shilman/8856ea1786dcd247139b47b270912324#upgrade
  core: {
    builder: "webpack5",
  },
  stories: ['../stories/*.stories.tsx'],
  addons: [
    '@storybook/addon-knobs',
  ],
  typescript: {
    check: false,
    checkOptions: {},
    reactDocgen: 'react-docgen-typescript'
  },
};
