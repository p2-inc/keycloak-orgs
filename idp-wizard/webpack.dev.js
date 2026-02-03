const path = require("path");
const { merge } = require("webpack-merge");
const common = require("./webpack.common.js");
const { stylePaths } = require("./stylePaths");

const HOST = process.env.HOST || "localhost";
const PORT = process.env.PORT || "9090";

module.exports = merge(common("development"), {
  mode: "development",
  devtool: "eval-source-map",
  devServer: {
    static: {
      directory: path.resolve(__dirname, "dist"),
      publicPath: "/", // optional, ensures assets resolve correctly
    },
    host: HOST,
    port: PORT,
    compress: true,
    historyApiFallback: true,
    hot: true, // 🔥 explicitly enable HMR
    client: {
      overlay: {
        warnings: false,
        errors: true,
      },
      logging: "info",
    },
    open: true,
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        include: [...stylePaths],
        use: ["style-loader", "css-loader", "postcss-loader"],
      },
    ],
  },
});
