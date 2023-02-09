/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      colors: {
        p2blue: {
          100: "#F9FCFE",
          200: "#EDF5FB",
          500: "#5B9FDD",
          700: "#1570C2",
          900: "#3C474E",
        },
        p2gray: {
          800: "#1C1D1E",
          900: "#252627",
        },
        p2grad: {
          100: "#A0BDEC",
          200: "#295398",
        },
      },
      dropShadow: {
        "btn-dark": "0px 1px 10px rgba(11, 25, 35, 0.5)",
        "btn-light": "0px 1px 10px rgba(11, 25, 35, 0.2)",
      },
    },
  },
  plugins: [require("@tailwindcss/forms")],
  future: {
    hoverOnlyWhenSupported: true,
  },
};
