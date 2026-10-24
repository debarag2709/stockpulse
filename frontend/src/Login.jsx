
import { useState } from 'react'
import api from './api'

function Login() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const handleLogin = async (event) => {
        event.preventDefault()
        setError('')
        setLoading(true)

        try {
            const response = await api.post('/api/auth/login', {
                email,
                password,
            })

            const { accessToken } = response.data

            if (!accessToken) {
                throw new Error('Login response did not contain accessToken.')
            }

            localStorage.setItem('accessToken', accessToken)
            window.location.href = '/'
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.message ||
                'Login failed. Please check your credentials.'
            )
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="login-container">
            <div className="login-card">
                <div className="login-brand">
                    <div className="brand-icon">S</div>
                    <span>
            stock<span className="brand-accent">pulse</span>
          </span>
                </div>

                <h1>Welcome back</h1>
                <p className="login-subtitle">
                    Sign in to access your StockPulse dashboard.
                </p>

                <form onSubmit={handleLogin} className="login-form">
                    <div className="login-field">
                        <label htmlFor="email">Email address</label>
                        <input
                            className="login-input"
                            id="email"
                            type="email"
                            placeholder="you@example.com"
                            value={email}
                            onChange={(event) => setEmail(event.target.value)}
                            autoComplete="email"
                            required
                        />
                    </div>

                    <div className="login-field">
                        <label htmlFor="password">Password</label>
                        <input
                            className="login-input"
                            id="password"
                            type="password"
                            placeholder="Enter your password"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                            autoComplete="current-password"
                            required
                        />
                    </div>

                    {error && (
                        <p className="login-error" role="alert">
                            {error}
                        </p>
                    )}

                    <button
                        className="login-button"
                        type="submit"
                        disabled={loading}
                    >
                        {loading ? 'Signing in...' : 'Sign in'}
                    </button>
                </form>

                <p className="login-footer">
                    Secure access to your stock monitoring workspace.
                </p>
            </div>
        </div>
    )
}

export default Login
