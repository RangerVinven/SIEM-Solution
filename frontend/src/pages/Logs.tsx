import { useState } from "react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { getLogs } from "@/lib/api"

type LogDocument = {
    id: string
    timestamp: string
    message: string
    event?: Record<string, string>
    host?: Record<string, string>
    log?: Record<string, string>
}

type PageData = {
    content: LogDocument[]
    totalPages: number
    number: number
    totalElements: number
}

export default function Logs() {
    const [message, setMessage] = useState("")
    const [hostname, setHostname] = useState("")
    const [category, setCategory] = useState("")
    const [level, setLevel] = useState("")
    const [page, setPage] = useState(0)
    const [data, setData] = useState<PageData | null>(null)
    const [loading, setLoading] = useState(false)
    const [searched, setSearched] = useState(false)

    function search(p: number) {
        setLoading(true)
        setSearched(true)
        getLogs({ message: message || undefined, hostname: hostname || undefined, category: category || undefined, level: level || undefined, page: p, size: 25 })
            .then(setData)
            .finally(() => setLoading(false))
    }

    function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        setPage(0)
        search(0)
    }

    function handlePageChange(p: number) {
        setPage(p)
        search(p)
    }

    return (
        <div className="flex flex-col gap-6">
            <h1 className="text-2xl font-semibold">Logs</h1>

            <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-3 lg:grid-cols-4">
                <Input
                    placeholder="Message"
                    value={message}
                    onChange={e => setMessage(e.target.value)}
                />
                <Input
                    placeholder="Hostname"
                    value={hostname}
                    onChange={e => setHostname(e.target.value)}
                />
                <Input
                    placeholder="Category"
                    value={category}
                    onChange={e => setCategory(e.target.value)}
                />
                <Input
                    placeholder="Level"
                    value={level}
                    onChange={e => setLevel(e.target.value)}
                />
                <Button type="submit" className="col-span-2 lg:col-span-4">
                    Search
                </Button>
            </form>

            {loading && <div className="text-muted-foreground text-sm">Loading...</div>}

            {!loading && searched && data && (
                <>
                    <div className="text-sm text-muted-foreground">
                        {data.totalElements} result{data.totalElements !== 1 ? "s" : ""}
                    </div>

                    {data.content.length === 0 ? (
                        <div className="text-muted-foreground text-sm">No logs found.</div>
                    ) : (
                        <div className="overflow-x-auto rounded-md border">
                            <table className="w-full text-sm">
                                <thead>
                                    <tr className="border-b bg-muted/50 text-left">
                                        <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">Timestamp</th>
                                        <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">Hostname</th>
                                        <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">Level</th>
                                        <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">Category</th>
                                        <th className="px-4 py-3 font-medium text-muted-foreground">Message</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y">
                                    {data.content.map(log => (
                                        <tr key={log.id} className="hover:bg-muted/30">
                                            <td className="px-4 py-3 whitespace-nowrap text-muted-foreground">
                                                {log.timestamp ? new Date(log.timestamp).toLocaleString() : "—"}
                                            </td>
                                            <td className="px-4 py-3 whitespace-nowrap">{log.host?.hostname ?? "—"}</td>
                                            <td className="px-4 py-3 whitespace-nowrap">{log.log?.level ?? "—"}</td>
                                            <td className="px-4 py-3 whitespace-nowrap">{log.event?.category ?? "—"}</td>
                                            <td className="px-4 py-3 max-w-md truncate">{log.message ?? "—"}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {data.totalPages > 1 && (
                        <div className="flex items-center justify-between">
                            <Button
                                variant="outline"
                                size="sm"
                                disabled={page === 0}
                                onClick={() => handlePageChange(page - 1)}
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
                                onClick={() => handlePageChange(page + 1)}
                            >
                                Next
                            </Button>
                        </div>
                    )}
                </>
            )}
        </div>
    )
}
