import Layout from "components/layouts/layout";
import React from "react";
import { Outlet } from "react-router-dom";
import "./App.css";

function App() {
  return (
    <Layout>
      <Outlet />
    </Layout>
  );
}

export default App;
