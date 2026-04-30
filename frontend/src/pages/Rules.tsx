import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { ChevronDown, ChevronUp, Plus, Trash2, X } from "lucide-react"
import { getRules, createRule, updateRule, deleteRule } from "@/lib/api"

type Rule = {
    id: number
    name: string
    description: string
    severity: string
    fieldToWatch: string
    expectedValue: string
    threshold: number
    windowMinutes: number
    remediationSteps: string[]
    enabled: boolean
}

type RuleForm = {
    name: string
    description: string
    severity: string
    fieldToWatch: string
    expectedValue: string
    threshold: number
    windowMinutes: number
    remediationSteps: string[]
    enabled: boolean
}

const emptyForm: RuleForm = {
    name: "",
    description: "",
    severity: "MEDIUM",
    fieldToWatch: "",
    expectedValue: "",
    threshold: 1,
    windowMinutes: 5,
    remediationSteps: [],
    enabled: true,
}

const severityStyles: Record<string, string> = {
    HIGH: "bg-red-100 text-red-700",
    MEDIUM: "bg-amber-100 text-amber-700",
    LOW: "bg-blue-100 text-blue-700",
}

export default function Rules() {
    const [rules, setRules] = useState<Rule[]>([])
    const [loading, setLoading] = useState(true)
    const [expandedId, setExpandedId] = useState<number | null>(null)
    const [showForm, setShowForm] = useState(false)
    const [editingId, setEditingId] = useState<number | null>(null)
    const [form, setForm] = useState<RuleForm>(emptyForm)
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState("")

    useEffect(() => {
        fetchRules()
    }, [])

    function fetchRules() {
        setLoading(true)
        getRules()
            .then(setRules)
            .finally(() => setLoading(false))
    }

    function openCreate() {
        setEditingId(null)
        setForm(emptyForm)
        setError("")
        setShowForm(true)
    }

    function openEdit(rule: Rule) {
        setEditingId(rule.id)
        setForm({
            name: rule.name,
            description: rule.description ?? "",
            severity: rule.severity,
            fieldToWatch: rule.fieldToWatch,
            expectedValue: rule.expectedValue,
            threshold: rule.threshold,
            windowMinutes: rule.windowMinutes,
            remediationSteps: rule.remediationSteps ?? [],
            enabled: rule.enabled,
        })
        setError("")
        setShowForm(true)
        setExpandedId(null)
        window.scrollTo({ top: 0, behavior: "smooth" })
    }

    function closeForm() {
        setShowForm(false)
        setEditingId(null)
        setForm(emptyForm)
        setError("")
    }

    function setField<K extends keyof RuleForm>(key: K, value: RuleForm[K]) {
        setForm(f => ({ ...f, [key]: value }))
    }

    function addStep() {
        setField("remediationSteps", [...form.remediationSteps, ""])
    }

    function updateStep(index: number, value: string) {
        const updated = form.remediationSteps.map((s, i) => i === index ? value : s)
        setField("remediationSteps", updated)
    }

    function removeStep(index: number) {
        setField("remediationSteps", form.remediationSteps.filter((_, i) => i !== index))
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        setError("")
        setSaving(true)
        try {
            const payload = {
                ...form,
                remediationSteps: form.remediationSteps.filter(s => s.trim() !== ""),
            }
            if (editingId !== null) {
                await updateRule(editingId, payload)
            } else {
                await createRule(payload)
            }
            closeForm()
            fetchRules()
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "Failed to save rule")
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete(id: number) {
        if (!confirm("Delete this rule?")) return
        await deleteRule(id)
        fetchRules()
    }

    return (
        <div className="flex flex-col gap-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-semibold">Rules</h1>
                {!showForm && (
                    <Button size="sm" onClick={openCreate}>
                        <Plus size={15} className="mr-1" />
                        New Rule
                    </Button>
                )}
            </div>

            {showForm && (
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-4">
                        <CardTitle className="text-base">
                            {editingId !== null ? "Edit Rule" : "New Rule"}
                        </CardTitle>
                        <Button variant="ghost" size="sm" onClick={closeForm}>
                            <X size={16} />
                        </Button>
                    </CardHeader>
                    <CardContent>
                        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
                            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                                <div className="grid gap-2">
                                    <Label htmlFor="name">Name</Label>
                                    <Input
                                        id="name"
                                        value={form.name}
                                        onChange={e => setField("name", e.target.value)}
                                        required
                                    />
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="severity">Severity</Label>
                                    <select
                                        id="severity"
                                        value={form.severity}
                                        onChange={e => setField("severity", e.target.value)}
                                        className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
                                    >
                                        <option value="HIGH">High</option>
                                        <option value="MEDIUM">Medium</option>
                                        <option value="LOW">Low</option>
                                    </select>
                                </div>
                                <div className="grid gap-2 sm:col-span-2">
                                    <Label htmlFor="description">Description</Label>
                                    <Input
                                        id="description"
                                        value={form.description}
                                        onChange={e => setField("description", e.target.value)}
                                    />
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="fieldToWatch">Field to Watch</Label>
                                    <Input
                                        id="fieldToWatch"
                                        placeholder="e.g. event.category"
                                        value={form.fieldToWatch}
                                        onChange={e => setField("fieldToWatch", e.target.value)}
                                        required
                                    />
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="expectedValue">Expected Value</Label>
                                    <Input
                                        id="expectedValue"
                                        placeholder="e.g. authentication"
                                        value={form.expectedValue}
                                        onChange={e => setField("expectedValue", e.target.value)}
                                        required
                                    />
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="threshold">Threshold</Label>
                                    <Input
                                        id="threshold"
                                        type="number"
                                        min={1}
                                        value={form.threshold}
                                        onChange={e => setField("threshold", Number(e.target.value))}
                                        required
                                    />
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="windowMinutes">Window (minutes)</Label>
                                    <Input
                                        id="windowMinutes"
                                        type="number"
                                        min={1}
                                        value={form.windowMinutes}
                                        onChange={e => setField("windowMinutes", Number(e.target.value))}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="flex flex-col gap-3">
                                <div className="flex items-center justify-between">
                                    <Label>Remediation Steps</Label>
                                    <Button type="button" variant="outline" size="sm" onClick={addStep}>
                                        <Plus size={14} className="mr-1" />
                                        Add Step
                                    </Button>
                                </div>
                                {form.remediationSteps.length === 0 && (
                                    <p className="text-sm text-muted-foreground">No steps added.</p>
                                )}
                                {form.remediationSteps.map((step, i) => (
                                    <div key={i} className="flex gap-2 items-center">
                                        <span className="text-sm text-muted-foreground w-5 shrink-0">{i + 1}.</span>
                                        <Input
                                            value={step}
                                            onChange={e => updateStep(i, e.target.value)}
                                            placeholder={`Step ${i + 1}`}
                                        />
                                        <Button type="button" variant="ghost" size="sm" onClick={() => removeStep(i)}>
                                            <Trash2 size={14} />
                                        </Button>
                                    </div>
                                ))}
                            </div>

                            <div className="flex items-center gap-2">
                                <input
                                    id="enabled"
                                    type="checkbox"
                                    checked={form.enabled}
                                    onChange={e => setField("enabled", e.target.checked)}
                                    className="h-4 w-4 rounded border-input"
                                />
                                <Label htmlFor="enabled">Enabled</Label>
                            </div>

                            {error && <p className="text-sm text-red-500">{error}</p>}

                            <div className="flex gap-2">
                                <Button type="submit" disabled={saving}>
                                    {saving ? "Saving..." : editingId !== null ? "Save Changes" : "Create Rule"}
                                </Button>
                                <Button type="button" variant="outline" onClick={closeForm}>
                                    Cancel
                                </Button>
                            </div>
                        </form>
                    </CardContent>
                </Card>
            )}

            {loading ? (
                <div className="text-muted-foreground text-sm">Loading...</div>
            ) : rules.length === 0 ? (
                <div className="text-muted-foreground text-sm">No rules found.</div>
            ) : (
                <div className="flex flex-col gap-2">
                    {rules.map(rule => (
                        <Card key={rule.id}>
                            <CardContent className="p-0">
                                <div
                                    className="flex items-center justify-between px-4 py-3 cursor-pointer"
                                    onClick={() => setExpandedId(expandedId === rule.id ? null : rule.id)}
                                >
                                    <div className="flex items-center gap-3 min-w-0">
                                        <span className={`shrink-0 text-xs font-medium px-2 py-0.5 rounded-full ${severityStyles[rule.severity] ?? "bg-muted text-muted-foreground"}`}>
                                            {rule.severity}
                                        </span>
                                        <span className="text-sm font-medium truncate">{rule.name}</span>
                                        {!rule.enabled && (
                                            <span className="text-xs text-muted-foreground shrink-0">Disabled</span>
                                        )}
                                    </div>
                                    <div className="shrink-0 ml-4">
                                        {expandedId === rule.id ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                                    </div>
                                </div>

                                {expandedId === rule.id && (
                                    <div className="border-t px-4 py-4 flex flex-col gap-4">
                                        {rule.description && (
                                            <div className="flex flex-col gap-1">
                                                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Description</p>
                                                <p className="text-sm">{rule.description}</p>
                                            </div>
                                        )}
                                        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
                                            <div className="flex flex-col gap-1">
                                                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Field</p>
                                                <p className="text-sm font-mono">{rule.fieldToWatch}</p>
                                            </div>
                                            <div className="flex flex-col gap-1">
                                                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Value</p>
                                                <p className="text-sm font-mono">{rule.expectedValue}</p>
                                            </div>
                                            <div className="flex flex-col gap-1">
                                                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Threshold</p>
                                                <p className="text-sm">{rule.threshold}</p>
                                            </div>
                                            <div className="flex flex-col gap-1">
                                                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Window</p>
                                                <p className="text-sm">{rule.windowMinutes}m</p>
                                            </div>
                                        </div>
                                        {rule.remediationSteps?.length > 0 && (
                                            <div className="flex flex-col gap-1">
                                                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Remediation Steps</p>
                                                <ol className="flex flex-col gap-1 list-decimal list-inside">
                                                    {rule.remediationSteps.map((step, i) => (
                                                        <li key={i} className="text-sm">{step}</li>
                                                    ))}
                                                </ol>
                                            </div>
                                        )}
                                        <div className="flex gap-2">
                                            <Button size="sm" variant="outline" onClick={() => openEdit(rule)}>
                                                Edit
                                            </Button>
                                            <Button size="sm" variant="destructive" onClick={() => handleDelete(rule.id)}>
                                                Delete
                                            </Button>
                                        </div>
                                    </div>
                                )}
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    )
}
