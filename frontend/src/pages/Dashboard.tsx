import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { getAlertStats, getAlerts, getAgents } from "@/lib/api"

type AlertStats = {
    high: number
    medium: number
    low: number
    totalUnresolved: number
}

type Alert = {
    id: number
    ruleName: string
    severity: string
    hostName: string
    location: string
    timestamp: string
}

type Agent = {
    id: string
    status: string
}

const severityStyles: Record<string, string> = {
    HIGH: "bg-red-100 text-red-700",
    MEDIUM: "bg-amber-100 text-amber-700",
    LOW: "bg-blue-100 text-blue-700",
}

function formatTimestamp(ts: string) {
    return new Date(ts).toLocaleString()
}

export default function Dashboard() {
    const [stats, setStats] = useState<AlertStats | null>(null)
    const [recentAlerts, setRecentAlerts] = useState<Alert[]>([])
    const [agents, setAgents] = useState<Agent[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        Promise.all([
            getAlertStats(),
            getAlerts({ resolved: false, page: 0, size: 5 }),
            getAgents(),
        ]).then(([statsData, alertsData, agentsData]) => {
            setStats(statsData)
            setRecentAlerts(alertsData.content)
            setAgents(agentsData)
        }).finally(() => setLoading(false))
    }, [])

    const onlineCount = agents.filter(a => a.status === "ONLINE").length
    const offlineCount = agents.filter(a => a.status === "OFFLINE").length

    if (loading) {
        return <div className="text-muted-foreground text-sm">Loading...</div>
    }

    return (
        <div className="flex flex-col gap-8">
            <h1 className="text-2xl font-semibold">Dashboard</h1>

            <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium text-muted-foreground">High Alerts</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-3xl font-bold text-red-600">{stats?.high ?? 0}</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium text-muted-foreground">Medium Alerts</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-3xl font-bold text-amber-500">{stats?.medium ?? 0}</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium text-muted-foreground">Low Alerts</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-3xl font-bold text-blue-500">{stats?.low ?? 0}</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium text-muted-foreground">Total Unresolved</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-3xl font-bold">{stats?.totalUnresolved ?? 0}</p>
                    </CardContent>
                </Card>
            </div>

            <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
                <div className="lg:col-span-2">
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-base">Recent Unresolved Alerts</CardTitle>
                        </CardHeader>
                        <CardContent>
                            {recentAlerts.length === 0 ? (
                                <p className="text-sm text-muted-foreground">No unresolved alerts.</p>
                            ) : (
                                <div className="flex flex-col divide-y">
                                    {recentAlerts.map(alert => (
                                        <div key={alert.id} className="flex items-center justify-between py-3">
                                            <div className="flex flex-col gap-1">
                                                <span className="text-sm font-medium">{alert.ruleName}</span>
                                                <span className="text-xs text-muted-foreground">
                                                    {alert.hostName}{alert.location ? ` · ${alert.location}` : ""}
                                                </span>
                                            </div>
                                            <div className="flex items-center gap-3 shrink-0">
                                                <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${severityStyles[alert.severity] ?? "bg-muted text-muted-foreground"}`}>
                                                    {alert.severity}
                                                </span>
                                                <span className="text-xs text-muted-foreground">
                                                    {formatTimestamp(alert.timestamp)}
                                                </span>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </div>

                <Card>
                    <CardHeader>
                        <CardTitle className="text-base">Agents</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <div className="flex items-center justify-between">
                            <span className="text-sm text-muted-foreground">Online</span>
                            <span className="text-sm font-semibold text-green-600">{onlineCount}</span>
                        </div>
                        <div className="flex items-center justify-between">
                            <span className="text-sm text-muted-foreground">Offline</span>
                            <span className="text-sm font-semibold text-red-600">{offlineCount}</span>
                        </div>
                        <div className="flex items-center justify-between border-t pt-4">
                            <span className="text-sm text-muted-foreground">Total</span>
                            <span className="text-sm font-semibold">{agents.length}</span>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    )
}
