const path = require('path');
const TsconfigPathsPlugin = require('tsconfig-paths-webpack-plugin');
const appConfig = require('../webpack.common');
const { stylePaths } = require("../stylePaths");

module.exports = ({ config, mode }) => {
  config.module.rules = [];
  config.module.rules.push(...appConfig(mode).module.rules);
  config.module.rules.push({
    test: /\.css$/,
    include: [
      path.resolve(__dirname, '../node_modules/@storybook'),
      ...stylePaths
    ],
    use: ["style-loader", "css-loader"]
  });
  config.module.rules.push({
    test: /\.tsx?$/,
    include: path.resolve(__dirname, '../src'),
    use: [
      require.resolve('react-docgen-typescript-loader'),
    ],
  })
  config.resolve.plugins = [
    new TsconfigPathsPlugin({
      configFile: path.resolve(__dirname, "../tsconfig.json")
    })
  ];
  config.resolve.extensions.push('.ts', '.tsx');
  return config;
};
