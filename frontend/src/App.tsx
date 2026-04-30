import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom"
import Login from "@/pages/Login"
import Signup from "@/pages/Signup"
import Onboarding from "@/pages/Onboarding"
import Layout from "@/components/Layout"
import Dashboard from "@/pages/Dashboard"
import Alerts from "@/pages/Alerts"
import Logs from "@/pages/Logs"
import Agents from "@/pages/Agents"
import Rules from "@/pages/Rules"
import Settings from "@/pages/Settings"
import Root from "@/pages/Root"

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/onboarding" element={<Onboarding />} />
                <Route element={<Layout />}>
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/alerts" element={<Alerts />} />
                    <Route path="/logs" element={<Logs />} />
                    <Route path="/agents" element={<Agents />} />
                    <Route path="/rules" element={<Rules />} />
                    <Route path="/settings" element={<Settings />} />
                </Route>
                <Route path="/" element={<Root />} />
                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App
