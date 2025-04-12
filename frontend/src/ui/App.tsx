import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Calendar from './Calendar';
import Home from '../Home'; // Főoldal, ha létezik

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/calendar" element={<div><h1>Naptár</h1><Calendar /></div>} />
    </Routes>
  );
};

export default App;