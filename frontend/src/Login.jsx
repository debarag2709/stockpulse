
import { useState } from 'react'
import api from './api'

function Login() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const [isRegistering, setIsRegistering] = useState(false)

    const handleSubmit = async (event) => {
        event.preventDefault()
        setError('')
        setLoading(true)

        try {
            if (isRegistering) {
                await api.post('/api/auth/register', {
                    email,
                    password,
                })

                setIsRegistering(false)
                setPassword('')
                setError('Registration successful! Please sign in.')
            } else {
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
            }
        } catch (err) {
            setError(
                err.response?.data?.detail ||
                err.response?.data?.message ||
                err.message ||
                (isRegistering ? 'Registration failed.' : 'Login failed.')
            )
        } finally {
            setLoading(false)
        }
    }

    const switchMode = () => {
        setIsRegistering(!isRegistering)
        setError('')
        setPassword('')
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

                <h1>{isRegistering ? 'Create your account' : 'Welcome back'}</h1>

                <p className="login-subtitle">
                    {isRegistering
                        ? 'Register to start monitoring your stocks.'
                        : 'Sign in to access your StockPulse dashboard.'}
                </p>

                <form onSubmit={handleSubmit} className="login-form">
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
                            autoComplete={isRegistering ? 'new-password' : 'current-password'}
                            minLength={isRegistering ? 8 : undefined}
                            maxLength={72}
                            required
                        />
                    </div>

                    {error && (
                        <p
                            className="login-error"
                            role="status"
                            aria-live="polite"
                        >
                            {error}
                        </p>
                    )}

                    <button
                        className="login-button"
                        type="submit"
                        disabled={loading}
                    >
                        {loading
                            ? (isRegistering ? 'Creating account...' : 'Signing in...')
                            : (isRegistering ? 'Register' : 'Sign in')}
                    </button>
                </form>

                <p className="login-footer">
                    {isRegistering ? 'Already have an account? ' : 'New to StockPulse? '}
                    <button
                        type="button"
                        className="login-mode-button"
                        onClick={switchMode}
                        disabled={loading}
                    >
                        {isRegistering ? 'Sign in' : 'Create account'}
                    </button>
                </p>

                {!isRegistering && (
                    <p className="login-footer">
                        Secure access to your stock monitoring workspace.
                    </p>
                )}
            </div>
        </div>
    )
}

export default Login
