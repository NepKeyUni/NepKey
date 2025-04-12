import React from 'react';
import { Link } from 'react-router-dom';

const Home: React.FC = () => {
  return (
    <div>
      <h1>Főoldal</h1>
      <Link to="/calendar">Naptár megtekintése</Link>
    </div>
  );
};

export default Home;