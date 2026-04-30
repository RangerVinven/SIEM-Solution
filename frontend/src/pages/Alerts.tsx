import { useEffect, useState } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { ChevronDown, ChevronUp } from "lucide-react"
import { getAlerts, resolveAlert, unresolveAlert } from "@/lib/api"

type Alert = {
    id: number
    ruleName: string
    severity: string
    description: string
    remediationSteps: string[]
    hostName: string
    location: string
    timestamp: string
    resolved: boolean
    resolvedAt: string | null
}

type PageData = {
    content: Alert[]
    totalPages: number
    number: number
}

const severityStyles: Record<string, string> = {
    HIGH: "bg-red-100 text-red-700",
    MEDIUM: "bg-amber-100 text-amber-700",
    LOW: "bg-blue-100 text-blue-700",
}

type Filter = "unresolved" | "resolved" | "all"

export default function Alerts() {
    const [filter, setFilter] = useState<Filter>("unresolved")
    const [page, setPage] = useState(0)
    const [data, setData] = useState<PageData | null>(null)
    const [expandedId, setExpandedId] = useState<number | null>(null)
    const [loading, setLoading] = useState(true)

    function fetchAlerts(f: Filter, p: number) {
        setLoading(true)
        const params: { resolved?: boolean; page: number; size: number } = { page: p, size: 10 }
        if (f === "resolved") params.resolved = true
        if (f === "unresolved") params.resolved = false
        getAlerts(params)
            .then(setData)
            .finally(() => setLoading(false))
    }

    useEffect(() => {
        fetchAlerts(filter, page)
    }, [filter, page])

    function handleFilterChange(f: Filter) {
        setFilter(f)
        setPage(0)
        setExpandedId(null)
    }

    async function handleResolve(alert: Alert) {
        if (alert.resolved) {
            await unresolveAlert(alert.id)
        } else {
            await resolveAlert(alert.id)
        }
        fetchAlerts(filter, page)
    }

    const filterOptions: { label: string; value: Filter }[] = [
        { label: "Unresolved", value: "unresolved" },
        { label: "Resolved", value: "resolved" },
        { label: "All", value: "all" },
    ]

    return (
        <div className="flex flex-col gap-6">
            <h1 className="text-2xl font-semibold">Alerts</h1>

            <div className="flex gap-2">
                {filterOptions.map(opt => (
                    <Button
                        key={opt.value}
                        variant={filter === opt.value ? "default" : "outline"}
                        size="sm"
                        onClick={() => handleFilterChange(opt.value)}
                    >
                        {opt.label}
                    </Button>
                ))}
            </div>

            {loading ? (
                <div className="text-muted-foreground text-sm">Loading...</div>
            ) : !data || data.content.length === 0 ? (
                <div className="text-muted-foreground text-sm">No alerts found.</div>
            ) : (
                <div className="flex flex-col gap-2">
                    {data.content.map(alert => (
                        <Card key={alert.id}>
                            <CardContent className="p-0">
                                <div
                                    className="flex items-center justify-between px-4 py-3 cursor-pointer"
                                    onClick={() => setExpandedId(expandedId === alert.id ? null : alert.id)}
                                >
                                    <div className="flex items-center gap-3 min-w-0">
                                        <span className={`shrink-0 text-xs font-medium px-2 py-0.5 rounded-full ${severityStyles[alert.severity] ?? "bg-muted text-muted-foreground"}`}>
                                            {alert.severity}
                                        </span>
                                        <span className="text-sm font-medium truncate">{alert.ruleName}</span>
                                        <span className="text-xs text-muted-foreground truncate hidden sm:block">
                                            {alert.hostName}{alert.location ? ` · ${alert.location}` : ""}
                                        </span>
                                    </div>
                                    <div className="flex items-center gap-3 shrink-0 ml-4">
                                        <span className="text-xs text-muted-foreground hidden md:block">
                                            {new Date(alert.timestamp).toLocaleString()}
                                        </span>
                                        {expandedId === alert.id ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                                    </div>
                                </div>

                                {expandedId === alert.id && (
                                    <div className="border-t px-4 py-4 flex flex-col gap-4">
                                        <div className="flex flex-col gap-1">
                                            <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Host</p>
                                            <p className="text-sm">{alert.hostName}{alert.location ? ` · ${alert.location}` : ""}</p>
                                        </div>
                                        <div className="flex flex-col gap-1">
                                            <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Description</p>
                                            <p className="text-sm">{alert.description}</p>
                                        </div>
                                        {alert.remediationSteps?.length > 0 && (
                                            <div className="flex flex-col gap-1">
                                                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Remediation Steps</p>
                                                <ol className="flex flex-col gap-1 list-decimal list-inside">
                                                    {alert.remediationSteps.map((step, i) => (
                                                        <li key={i} className="text-sm">{step}</li>
                                                    ))}
                                                </ol>
                                            </div>
                                        )}
                                        {alert.resolvedAt && (
                                            <div className="flex flex-col gap-1">
                                                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Resolved At</p>
                                                <p className="text-sm">{new Date(alert.resolvedAt).toLocaleString()}</p>
                                            </div>
                                        )}
                                        <div>
                                            <Button
                                                size="sm"
                                                variant={alert.resolved ? "outline" : "default"}
                                                onClick={e => { e.stopPropagation(); handleResolve(alert) }}
                                            >
                                                {alert.resolved ? "Mark as Unresolved" : "Mark as Resolved"}
                                            </Button>
                                        </div>
                                    </div>
                                )}
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}

            {data && data.totalPages > 1 && (
                <div className="flex items-center justify-between pt-2">
                    <Button
                        variant="outline"
                        size="sm"
                        disabled={page === 0}
                        onClick={() => setPage(p => p - 1)}
                    >
                        Previous
                    </Button>
                    <span className="text-sm text-muted-foreground">
                        Page {data.number + 1} of {data.totalPages}
                    </span>
                    <Button
                        variant="outline"
                        size="sm"
                        disabled={page >= data.totalPages - 1}
                        onClick={() => setPage(p => p + 1)}
                    >
                        Next
                    </Button>
                </div>
            )}
        </div>
    )
}
