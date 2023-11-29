/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: "class",
  content: ["./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      colors: {
        primary: {
          100: "#F9FCFE",
          200: "#EDF5FB",
          400: "#739FE5",
          500: "#5B9FDD",
          600: "#295398",
          700: "#1570C2",
          900: "#3C474E",
        },
        secondary: {
          800: "#1C1D1E",
          900: "#252627",
        },
        p2dark: {
          900: "#111111",
          1000: "#000000",
        },
      },
      dropShadow: {
        "btn-dark": "0px 1px 8px rgba(11, 25, 35, 0.4)",
        "btn-light": "0px 1px 8px rgba(11, 25, 35, 0.1)",
      },
    },
  },
  plugins: [require("@tailwindcss/forms")],
  future: {
    hoverOnlyWhenSupported: true,
  },
};
