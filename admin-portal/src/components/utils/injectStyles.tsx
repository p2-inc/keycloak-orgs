import { useEffect } from "react";
import { config } from "config";

function generateColorStyles(colorName: string, colorValue?: string) {
  return colorValue
    ? `.text-${colorName} { color: ${colorValue}; }
       .bg-${colorName} { background-color: ${colorValue}; }`
    : "";
}

const InjectStyles = () => {
  useEffect(() => {
    const { styles = { customCSS: "" } } = config.env;
    const styleElement = document.createElement("style");

    const primaryColor = `
      ${generateColorStyles("primary-100", styles.primary100)}
      ${generateColorStyles("primary-200", styles.primary200)}
      ${generateColorStyles("primary-400", styles.primary400)}
      ${generateColorStyles("primary-500", styles.primary500)}
      ${generateColorStyles("primary-600", styles.primary600)}
      ${generateColorStyles("primary-700", styles.primary700)}
      ${generateColorStyles("primary-900", styles.primary900)}
    `;

    const secondaryColor = `
      ${generateColorStyles("secondary-800", styles.secondary800)}
      ${generateColorStyles("secondary-900", styles.secondary900)}
    `;

    const customStyles = `
      ${primaryColor}
      ${secondaryColor}
      ${styles.customCSS || ""}
    `
      .replace(/\r?\n/g, " ")
      .replace(/\s+/g, " ")
      .trim();

    styleElement.innerHTML = customStyles;

    if (customStyles) {
      document.body.appendChild(styleElement);
    }

    return () => {
      document.body.removeChild(styleElement);
    };
  }, []);

  return null;
};

export default InjectStyles;
