import { useEffect, useState } from "react"
import { Navigate } from "react-router-dom"
import { getUser } from "@/lib/api"

export default function Root() {
    const [destination, setDestination] = useState<string | null>(null)

    useEffect(() => {
        getUser()
            .then(() => setDestination("/dashboard"))
            .catch(() => setDestination("/login"))
    }, [])

    if (!destination) return null

    return <Navigate to={destination} replace />
}
