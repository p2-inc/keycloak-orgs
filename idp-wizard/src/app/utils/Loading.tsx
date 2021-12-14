import React from 'react';

function keyframes() {
  return {
    __html: "@keyframes spin { 0% { transform: rotate(0deg) } 100% { transform: rotate(360deg) }}"
  };
}

function style(size, stroke, color) {
  return {
    inner: {
      animation: 'spin 0.5s linear infinite',
      height: size,
      width: size,
      border: `${stroke} solid transparent`,
      borderTopColor: color,
      borderRadius: '100%',
      boxSizing: 'border-box',
    },
    outer: {
      height: '100%',
      width: '100%',
      display: 'flex',
      flexFlow: 'column nowrap',
      justifyContent: 'center',
      alignItems: 'center'
    }
  };
}

const Loading = ({
  size = '50px',
  stroke = '3px',
  color = '#000'
}) => {
  const styles = style(size, stroke, color);
  return (
    <div style={styles.outer}>
      <div style={styles.inner} />
      <style dangerouslySetInnerHTML={keyframes()}/>
    </div>
  );
};

export default Loading;
