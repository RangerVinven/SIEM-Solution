import { useState } from "react"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { useNavigate } from "react-router-dom"
import { createSchool } from "@/lib/api"

export default function Onboarding() {
    const navigate = useNavigate()
    const [name, setName] = useState("")
    const [error, setError] = useState("")
    const [loading, setLoading] = useState(false)

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        setError("")
        setLoading(true)
        try {
            await createSchool(name)
            navigate("/dashboard")
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "Failed to create school")
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="flex min-h-screen items-center justify-center">
            <Card className="w-full max-w-sm">
                <CardHeader>
                    <CardTitle>Create Your School</CardTitle>
                    <CardDescription>Set up your school to get started.</CardDescription>
                </CardHeader>

                <CardContent>
                    <form id="onboarding-form" onSubmit={handleSubmit}>
                        <div className="flex flex-col gap-6">
                            <div className="grid gap-2">
                                <Label htmlFor="name">School Name</Label>
                                <Input
                                    id="name"
                                    type="text"
                                    placeholder="Greenfield Academy"
                                    value={name}
                                    onChange={e => setName(e.target.value)}
                                    required
                                />
                            </div>
                            {error && <p className="text-sm text-red-500">{error}</p>}
                        </div>
                    </form>
                </CardContent>

                <CardFooter>
                    <Button type="submit" form="onboarding-form" className="w-full" disabled={loading}>
                        {loading ? "Creating..." : "Create School"}
                    </Button>
                </CardFooter>
            </Card>
        </div>
    )
}
