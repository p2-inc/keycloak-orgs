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
          100: "#739FE5",
          200: "#295398",
        },
        p2dark: {
          900: '#111111',
          1000: '#000000',
        }
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
