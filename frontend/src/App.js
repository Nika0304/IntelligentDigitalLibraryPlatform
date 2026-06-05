import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Header from "./components/Header";
import Footer from "./components/Footer";
import Home from "./pages/Home";
import Catalog from "./pages/Catalog";
import BookDetail from "./pages/BookDetail";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Profile from "./pages/Profile";
import Admin from "./pages/Admin";
import { AuthProvider } from "./context/AuthContext";
import "./App.css";
import ChatWidget from "./components/ChatWidget";
import Groups from "./pages/Groups";
import GroupDetail from "./pages/GroupDetail";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="relative min-h-screen">
          <Header />
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/catalog" element={<Catalog />} />
            <Route path="/carte/:id" element={<BookDetail />} />
            <Route path="/autentificare" element={<Login />} />
            <Route path="/inregistrare" element={<Register />} />
            <Route path="/profil" element={<Profile />} />
            <Route path="/admin" element={<Admin />} />
              <Route path="/cercuri" element={<Groups />} />
              <Route path="/cercuri/:id" element={<GroupDetail />} />

            <Route path="*" element={<Home />} />
          </Routes>
            <ChatWidget />
          <Footer />
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}
