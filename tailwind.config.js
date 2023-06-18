 const defaultTheme = require("tailwindcss/defaultTheme");
 /** @type {import('tailwindcss').Config} */
 module.exports = {
   content: ["./src/**/*.clj"],
   theme: {
     extend: {},
     fontFamily: {
       sans: ["Iosevka Aile Web", ...defaultTheme.fontFamily.sans],
       serif: ["Iosevka Etoile Web", ...defaultTheme.fontFamily.serif],
     },
   },
   plugins: [require("@tailwindcss/typography")],
 };
