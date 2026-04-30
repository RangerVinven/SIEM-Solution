import { NavLink, Outlet } from "react-router-dom"
import { LayoutDashboard, Bell, ScrollText, Monitor, ShieldAlert, Settings as SettingsIcon } from "lucide-react"

const navItems = [
    { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
    { to: "/alerts", label: "Alerts", icon: Bell },
    { to: "/logs", label: "Logs", icon: ScrollText },
    { to: "/agents", label: "Agents", icon: Monitor },
    { to: "/rules", label: "Rules", icon: ShieldAlert },
]

export default function Layout() {
    return (
        <div className="flex min-h-screen">
            <aside className="w-56 shrink-0 border-r bg-background flex flex-col">
                <div className="h-16 flex items-center px-6 border-b">
                    <span className="font-semibold text-sm tracking-tight">Simple SIEM</span>
                </div>
                <nav className="flex flex-col gap-1 p-3 flex-1">
                    {navItems.map(({ to, label, icon: Icon }) => (
                        <NavLink
                            key={to}
                            to={to}
                            className={({ isActive }) =>
                                `flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-colors ${
                                    isActive
                                        ? "bg-accent text-accent-foreground font-medium"
                                        : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                                }`
                            }
                        >
                            <Icon size={16} />
                            {label}
                        </NavLink>
                    ))}
                </nav>
                <div className="p-3 border-t">
                    <NavLink
                        to="/settings"
                        className={({ isActive }) =>
                            `flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-colors ${
                                isActive
                                    ? "bg-accent text-accent-foreground font-medium"
                                    : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                            }`
                        }
                    >
                        <SettingsIcon size={16} />
                        Settings
                    </NavLink>
                </div>
            </aside>
            <main className="flex-1 p-8 overflow-auto">
                <Outlet />
            </main>
        </div>
    )
}
