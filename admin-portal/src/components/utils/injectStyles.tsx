import { useEffect } from "react";
import { config } from "config";

function generateColorStyles(colorName: string, colorValue?: string) {
  return colorValue
    ? `.text-${colorName} { color: ${colorValue}; }
       .bg-${colorName} { background-color: ${colorValue}; }
       .border-${colorName} { border-color: ${colorValue}; }
       .from-${colorName} { --tw-gradient-from: ${colorValue}; --tw-gradient-stops: var(--tw-gradient-from), var(--tw-gradient-to, ${colorValue}); }
       .via-${colorName} { --tw-gradient-stops: var(--tw-gradient-from), ${colorValue}, var(--tw-gradient-to, ${colorValue}); }
       .to-${colorName} { --tw-gradient-to: ${colorValue}; }
       .placeholder-${colorName} { color: ${colorValue}; opacity: 1; }
       .ring-${colorName} { --tw-ring-color: ${colorValue}; }
       .divide-${colorName} { border-color: ${colorValue}; }
       .hover\\:text-${colorName}:hover { color: ${colorValue}; }
       .hover\\:bg-${colorName}:hover { background-color: ${colorValue}; }
       .hover\\:border-${colorName}:hover { border-color: ${colorValue}; }
       .hover\\:ring-${colorName}:hover { --tw-ring-color: ${colorValue}; }
       .focus\\:text-${colorName}:focus { color: ${colorValue}; }
       .focus\\:bg-${colorName}:focus { background-color: ${colorValue}; }
       .focus\\:border-${colorName}:focus { border-color: ${colorValue}; }
       .focus\\:ring-${colorName}:focus { --tw-ring-color: ${colorValue}; }
       .active\\:text-${colorName}:active { color: ${colorValue}; }
       .active\\:bg-${colorName}:active { background-color: ${colorValue}; }
       .active\\:border-${colorName}:active { border-color: ${colorValue}; }
       .active\\:ring-${colorName}:active { --tw-ring-color: ${colorValue}; }
       .group:hover .group-hover\\:text-${colorName} { color: ${colorValue}; }
       .group:hover .group-hover\\:bg-${colorName} { background-color: ${colorValue}; }
       .group:hover .group-hover\\:border-${colorName} { border-color: ${colorValue}; }
       .group:hover .group-hover\\:ring-${colorName} { --tw-ring-color: ${colorValue}; }
       .group:focus .group-focus\\:text-${colorName} { color: ${colorValue}; }
       .group:focus .group-focus\\:bg-${colorName} { background-color: ${colorValue}; }
       .group:focus .group-focus\\:border-${colorName} { border-color: ${colorValue}; }
       .group:focus .group-focus\\:ring-${colorName} { --tw-ring-color: ${colorValue}; }
       .group:active .group-active\\:text-${colorName} { color: ${colorValue}; }
       .group:active .group-active\\:bg-${colorName} { background-color: ${colorValue}; }
       .group:active .group-active\\:border-${colorName} { border-color: ${colorValue}; }
       .group:active .group-active\\:ring-${colorName} { --tw-ring-color: ${colorValue}; }
       .group:enabled .group-enabled\\:text-${colorName} { color: ${colorValue}; }
       .group:enabled .group-enabled\\:bg-${colorName} { background-color: ${colorValue}; }
       .group:enabled .group-enabled\\:border-${colorName} { border-color: ${colorValue}; }
       .group:enabled .group-enabled\\:ring-${colorName} { --tw-ring-color: ${colorValue}; }
       .group:enabled:hover .group-enabled\\:group-hover\\:text-${colorName} { color: ${colorValue}; }
       .group:enabled:hover .group-enabled\\:group-hover\\:bg-${colorName} { background-color: ${colorValue}; }
       .group:enabled:hover .group-enabled\\:group-hover\\:border-${colorName} { border-color: ${colorValue}; }
       .group:enabled:hover .group-enabled\\:group-hover\\:ring-${colorName} { --tw-ring-color: ${colorValue}; } 
       `
    : "";
}

function generateButtonStyles(cv1: string, cv2: string) {
  return cv1 && cv2
    ? `.bg-primary-gradient {
        background-image: linear-gradient(to right, ${cv1}, ${cv2});
      }`
    : "";
}

const InjectStyles = () => {
  useEffect(() => {
    // const {
    //   styles = {
    //     customCSS: "",
    //     primary100: "#ff00de",
    //     primary200: "#bc00a3",
    //     primary400: "#8b0078",
    //     primary500: "#810070",
    //     primary600: "#5c0050",
    //     primary700: "#46003d",
    //     primary900: "#30002a",
    //   },
    // } = config.env;

    const styles = {
      customCSS: "",
      primary100: "#ff00de",
      primary200: "#bc00a3",
      primary400: "#8b0078",
      primary500: "#810070",
      primary600: "#5c0050",
      primary700: "#46003d",
      primary900: "#30002a",
      secondary800: "#ff1e00",
      secondary900: "#b81500",
    };
    const styleElement = document.createElement("style");

    const primaryColor = `
      ${generateColorStyles("primary-100", styles.primary100)}
      ${generateColorStyles("primary-200", styles.primary200)}
      ${generateColorStyles("primary-400", styles.primary400)}
      ${generateColorStyles("primary-500", styles.primary500)}
      ${generateColorStyles("primary-600", styles.primary600)}
      ${generateColorStyles("primary-700", styles.primary700)}
      ${generateColorStyles("primary-900", styles.primary900)}
      ${generateButtonStyles(styles.primary100, styles.primary600)}
    `;

    const secondaryColor = `
      ${generateColorStyles("secondary-800", styles.secondary800)}
      ${generateColorStyles("secondary-900", styles.secondary900)}
    `;

    const customStyles = `
      ${primaryColor}
      ${secondaryColor}
      ${styles.customCSS}
    `
      .replace(/\r?\n/g, " ")
      .replace(/\s+/g, " ")
      .trim();

    styleElement.innerHTML = customStyles;
    document.body.appendChild(styleElement);

    return () => {
      document.body.removeChild(styleElement);
    };
  }, []);

  return null;
};

export default InjectStyles;
