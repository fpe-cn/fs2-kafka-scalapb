const repoUrl = "https://github.com/fpe-cn/fs2-kafka-scalapb";

const apiUrl = "/fs2-kafka-scalapb/api/fs2/kafka/scalapb/index.html";

// See https://docusaurus.io/docs/site-config for available options.
const siteConfig = {
  title: "FS2 Kafka ScalaPB",
  tagline: "A Protobuf plugin for FS2 Kafka",
  url: "https://fpe-cn.github.io/fs2-kafka-scalapb",
  baseUrl: "/fs2-kafka-scalapb/",

  customDocsPath: "docs/target/mdoc",

  projectName: "fs2-kafka-scalapb",
  organizationName: "fpe-cn",

  headerLinks: [
    { href: apiUrl, label: "API Docs" },
    { doc: "overview", label: "Documentation" },
    { href: repoUrl, label: "GitHub" }
  ],

  headerIcon: "img/fs2-kafka.white.svg",
  titleIcon: "img/fs2-kafka.svg",
  favicon: "img/favicon.png",

  colors: {
    primaryColor: "#122932",
    secondaryColor: "#153243"
  },

  copyright: `Copyright © 2021-${new Date().getFullYear()} Financière des Paiements Eléctroniques Limited.`,

  highlight: { theme: "github" },

  onPageNav: "separate",

  separateCss: ["api"],

  cleanUrl: true,

  repoUrl,

  apiUrl
};

module.exports = siteConfig;
