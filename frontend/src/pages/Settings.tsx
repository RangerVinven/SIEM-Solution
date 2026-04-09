import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { getUser, getSchool, updateSchool, addEmployee, getUsersBySchool } from "@/lib/api"

type User = {
    id: string
    firstName: string
    lastName: string
    email: string
    role: string
    schoolId: string
}

type School = {
    id: string
    name: string
}

export default function Settings() {
    const [user, setUser] = useState<User | null>(null)
    const [school, setSchool] = useState<School | null>(null)
    const [employees, setEmployees] = useState<User[]>([])
    const [loading, setLoading] = useState(true)

    const [newSchoolName, setNewSchoolName] = useState("")
    const [updatingSchool, setUpdatingSchool] = useState(false)

    const [newEmployeeFirstName, setNewEmployeeFirstName] = useState("")
    const [newEmployeeLastName, setNewEmployeeLastName] = useState("")
    const [newEmployeeEmail, setNewEmployeeEmail] = useState("")
    const [newEmployeePassword, setNewEmployeePassword] = useState("")
    const [addingEmployee, setAddingEmployee] = useState(false)

    useEffect(() => {
        fetchData()
    }, [])

    async function fetchData() {
        setLoading(true)
        try {
            const u = await getUser()
            setUser(u)
            if (u.schoolId) {
                const [s, emps] = await Promise.all([
                    getSchool(u.schoolId),
                    getUsersBySchool(u.schoolId)
                ])
                setSchool(s)
                setNewSchoolName(s.name)
                setEmployees(emps)
            }
        } catch (e) {
            console.error(e)
        } finally {
            setLoading(false)
        }
    }

    async function handleUpdateSchool(e: React.FormEvent) {
        e.preventDefault()
        if (!school) return
        setUpdatingSchool(true)
        try {
            const updated = await updateSchool(school.id, newSchoolName)
            setSchool(updated)
        } catch (e) {
            console.error(e)
        } finally {
            setUpdatingSchool(false)
        }
    }

    async function handleAddEmployee(e: React.FormEvent) {
        e.preventDefault()
        if (!school) return
        setAddingEmployee(true)
        try {
            await addEmployee(school.id, {
                firstName: newEmployeeFirstName,
                lastName: newEmployeeLastName,
                email: newEmployeeEmail,
                password: newEmployeePassword,
                role: "TECHNICAL_ADMIN"
            })
            setNewEmployeeFirstName("")
            setNewEmployeeLastName("")
            setNewEmployeeEmail("")
            setNewEmployeePassword("")
            const emps = await getUsersBySchool(school.id)
            setEmployees(emps)
        } catch (e) {
            console.error(e)
        } finally {
            setAddingEmployee(false)
        }
    }

    if (loading) return <div>Loading...</div>

    return (
        <div className="flex flex-col gap-8 max-w-4xl">
            <h1 className="text-2xl font-semibold">Settings</h1>

            <section className="space-y-4">
                <h2 className="text-lg font-medium">School Management</h2>
                <form onSubmit={handleUpdateSchool} className="grid gap-4 p-6 rounded-md border bg-card shadow-sm">
                    <div className="grid gap-2">
                        <Label htmlFor="school-name">School Name</Label>
                        <div className="flex gap-2">
                            <Input
                                id="school-name"
                                value={newSchoolName}
                                onChange={e => setNewSchoolName(e.target.value)}
                                required
                            />
                            <Button type="submit" disabled={updatingSchool || newSchoolName === school?.name}>
                                {updatingSchool ? "Updating..." : "Update"}
                            </Button>
                        </div>
                    </div>
                </form>
            </section>

            <section className="space-y-4">
                <h2 className="text-lg font-medium">Team Members</h2>
                
                <form onSubmit={handleAddEmployee} className="grid gap-4 p-6 rounded-md border bg-card shadow-sm">
                    <h3 className="text-sm font-medium">Add New Employee</h3>
                    <div className="grid grid-cols-2 gap-4">
                        <div className="grid gap-2">
                            <Label htmlFor="first-name">First Name</Label>
                            <Input
                                id="first-name"
                                value={newEmployeeFirstName}
                                onChange={e => setNewEmployeeFirstName(e.target.value)}
                                required
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label htmlFor="last-name">Last Name</Label>
                            <Input
                                id="last-name"
                                value={newEmployeeLastName}
                                onChange={e => setNewEmployeeLastName(e.target.value)}
                                required
                            />
                        </div>
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="email">Email Address</Label>
                        <Input
                            id="email"
                            type="email"
                            value={newEmployeeEmail}
                            onChange={e => setNewEmployeeEmail(e.target.value)}
                            required
                        />
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="password">Initial Password</Label>
                        <Input
                            id="password"
                            type="password"
                            value={newEmployeePassword}
                            onChange={e => setNewEmployeePassword(e.target.value)}
                            required
                        />
                    </div>
                    <Button type="submit" disabled={addingEmployee} className="w-full sm:w-auto self-start">
                        {addingEmployee ? "Adding..." : "Add Employee"}
                    </Button>
                </form>

                <div className="overflow-x-auto rounded-md border mt-8">
                    <table className="w-full text-sm">
                        <thead>
                            <tr className="border-b bg-muted/50 text-left">
                                <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">Name</th>
                                <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">Email</th>
                                <th className="px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">Role</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y">
                            {employees.map(emp => (
                                <tr key={emp.id} className="hover:bg-muted/30">
                                    <td className="px-4 py-3 whitespace-nowrap">{emp.firstName} {emp.lastName}</td>
                                    <td className="px-4 py-3 whitespace-nowrap text-muted-foreground">{emp.email}</td>
                                    <td className="px-4 py-3 whitespace-nowrap uppercase text-xs font-semibold">
                                        {emp.role.replace(/_/g, " ")}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    )
}
