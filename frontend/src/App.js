import React, { useEffect, useState } from 'react';

// Main application component.  On load it calls the backend API to
// retrieve a message.  The API URL is injected at build time via the
// REACT_APP_API_URL environment variable defined in docker-compose.yml.
function App() {
  const [message, setMessage] = useState('Loading…');

  useEffect(() => {
    // Construct the endpoint URL using the environment variable defined at
    // build time.  If the request fails we surface the error to the user.
    const endpoint = `${process.env.REACT_APP_API_URL}/message`;
    fetch(endpoint)
      .then((response) => response.text())
      .then((data) => setMessage(data))
      .catch((error) => setMessage('Error: ' + error.toString()));
  }, []);

  return (
    <div className="App">
      <h1>Secure Multi‑Tier Deployment</h1>
      <p>{message}</p>
    </div>
  );
}

export default App;