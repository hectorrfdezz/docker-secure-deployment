import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

// Entry point for the React application.  React 18 uses the new
// createRoot API for rendering.
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);