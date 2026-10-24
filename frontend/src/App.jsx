import { useEffect, useState } from 'react'
import './App.css'
import Login from './Login.jsx'
import api from './api'
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

function App() {
  const [activePage, setActivePage] = useState('Overview')
  const [selectedStock, setSelectedStock] = useState(null)
  const [priceHistory, setPriceHistory] = useState([])
  const [historyLoading, setHistoryLoading] = useState(false)
  const [historyError, setHistoryError] = useState('')
  const [search, setSearch] = useState('')
  const [stocks, setStocks] = useState([])
  const [stocksLoading, setStocksLoading] = useState(true)
  const [stocksError, setStocksError] = useState('')

  const [watchlist, setWatchlist] = useState([])
  const [watchlistStockIds, setWatchlistStockIds] = useState([])
  const [watchlistLoading, setWatchlistLoading] = useState(false)
  const [watchlistError, setWatchlistError] = useState('')
  const [removingStockId, setRemovingStockId] = useState(null)
  const [alerts, setAlerts] = useState([])
  const [alertsLoading, setAlertsLoading] = useState(false)
  const [alertsError, setAlertsError] = useState('')
  const [creatingAlert, setCreatingAlert] = useState(false)

  // Declare token before any effect that uses it.
  const token = localStorage.getItem('accessToken')

  // Load stocks and their latest recorded prices.
  useEffect(() => {
    if (!token) {
      setStocksLoading(false)
      return
    }

    const fetchStocks = async () => {
      try {
        setStocksError('')

        const response = await api.get('/api/stocks')
        const data = response.data
        const stockList = Array.isArray(data) ? data : data.content

        if (!Array.isArray(stockList)) {
          throw new Error('Unexpected response format from the stocks API.')
        }

        const stocksWithPrices = await Promise.all(
            stockList.map(async (stock) => {
              try {
                const priceResponse = await api.get(
                    `/api/stocks/${stock.id}/prices/latest`
                )

                return {
                  ...stock,
                  latestPrice: priceResponse.data.price,
                  recordedAt: priceResponse.data.recordedAt,
                }
              } catch (error) {
                if (error.response?.status === 404) {
                  return {
                    ...stock,
                    latestPrice: null,
                    recordedAt: null,
                  }
                }

                throw error
              }
            })
        )

        setStocks(stocksWithPrices)
      } catch (error) {
        if (error.response?.status === 401) {
          localStorage.removeItem('accessToken')
          window.location.href = '/'
          return
        }

        setStocksError(
            error.response?.data?.detail ||
            error.response?.data?.message ||
            'Could not load stocks and prices. Please try again.'
        )
      } finally {
        setStocksLoading(false)
      }
    }

    fetchStocks()
  }, [token])

  // Load historical prices when a stock's details page opens.
  useEffect(() => {
    if (!token || activePage !== 'Stock Details' || !selectedStock) {
      return
    }

    let cancelled = false

    const fetchPriceHistory = async () => {
      setHistoryLoading(true)
      setHistoryError('')
      setPriceHistory([])

      try {
        const response = await api.get(
            `/api/stocks/${selectedStock.id}/prices/history`
        )
        const data = Array.isArray(response.data)
            ? response.data
            : response.data?.content

        if (!Array.isArray(data)) {
          throw new Error('The price history API returned an unexpected response.')
        }

        const chronologicalPrices = data
            .filter((item) => item.recordedAt && Number.isFinite(Number(item.price)))
            .map((item) => ({
              ...item,
              price: Number(item.price),
              chartTime: new Date(item.recordedAt).getTime(),
            }))
            .sort((a, b) => a.chartTime - b.chartTime)

        if (!cancelled) {
          setPriceHistory(chronologicalPrices)
        }
      } catch (error) {
        if (error.response?.status === 401) {
          localStorage.removeItem('accessToken')
          window.location.href = '/'
          return
        }

        if (!cancelled) {
          setHistoryError(
              error.response?.data?.detail ||
              error.response?.data?.message ||
              'Could not load price history. Please try again.'
          )
        }
      } finally {
        if (!cancelled) {
          setHistoryLoading(false)
        }
      }
    }

    fetchPriceHistory()
    return () => {
      cancelled = true
    }
  }, [token, activePage, selectedStock])

  // Load IDs of stocks already saved in the watchlist.
  useEffect(() => {
    if (!token) {
      return
    }

    const fetchWatchlistIds = async () => {
      try {
        const response = await api.get('/api/watchlist')
        const data = response.data

        if (Array.isArray(data)) {
          setWatchlistStockIds(data.map((item) => item.stockId))
        }
      } catch (error) {
        if (error.response?.status === 401) {
          localStorage.removeItem('accessToken')
          window.location.href = '/'
          return
        }

        console.error('Could not load watchlist IDs:', error)
      }
    }

    fetchWatchlistIds()
  }, [token])

  useEffect(() => {
    if (!token || activePage !== 'Price alerts') {
      return
    }

    const fetchAlerts = async () => {
      setAlertsLoading(true)
      setAlertsError('')

      try {
        const response = await api.get('/api/alerts')
        setAlerts(response.data)
      } catch (error) {
        if (error.response?.status === 401) {
          localStorage.removeItem('accessToken')
          window.location.href = '/'
          return
        }

        setAlertsError(
            error.response?.data?.detail ||
            error.response?.data?.message ||
            'Could not load price alerts. Please try again.'
        )
      } finally {
        setAlertsLoading(false)
      }
    }

    fetchAlerts()
  }, [token, activePage])

  // Load watchlist entries and their latest prices when the page opens.
  useEffect(() => {
    if (!token || activePage !== 'Watchlist') {
      return
    }

    const fetchWatchlist = async () => {
      setWatchlistLoading(true)
      setWatchlistError('')

      try {
        const response = await api.get('/api/watchlist')
        const data = response.data

        if (!Array.isArray(data)) {
          throw new Error('Unexpected response format from the watchlist API.')
        }

        setWatchlistStockIds(data.map((item) => item.stockId))

        const watchlistWithPrices = await Promise.all(
            data.map(async (item) => {
              try {
                const priceResponse = await api.get(
                    `/api/stocks/${item.stockId}/prices/latest`
                )

                return {
                  ...item,
                  latestPrice: priceResponse.data.price,
                  recordedAt: priceResponse.data.recordedAt,
                }
              } catch (error) {
                if (error.response?.status === 404) {
                  return {
                    ...item,
                    latestPrice: null,
                    recordedAt: null,
                  }
                }

                throw error
              }
            })
        )

        setWatchlist(watchlistWithPrices)
      } catch (error) {
        if (error.response?.status === 401) {
          localStorage.removeItem('accessToken')
          window.location.href = '/'
          return
        }

        setWatchlistError(
            error.response?.data?.detail ||
            error.response?.data?.message ||
            error.message ||
            'Could not load your watchlist. Please try again.'
        )
      } finally {
        setWatchlistLoading(false)
      }
    }

    fetchWatchlist()
  }, [token, activePage])

  const handleOpenStockDetails = (stock) => {
    setSelectedStock(stock)
    setActivePage('Stock Details')
    setSearch('')
  }

  // Add a stock to the watchlist.
  const handleAddToWatchlist = async (stockId) => {
    setWatchlistError('')

    try {
      await api.post(`/api/watchlist/${stockId}`)

      setWatchlistStockIds((currentIds) =>
          currentIds.includes(stockId) ? currentIds : [...currentIds, stockId]
      )

      setActivePage('Watchlist')
      setSearch('')
    } catch (error) {
      if (error.response?.status === 409) {
        setWatchlistStockIds((currentIds) =>
            currentIds.includes(stockId) ? currentIds : [...currentIds, stockId]
        )

        setWatchlistError('This stock is already in your watchlist.')
        setActivePage('Watchlist')
        setSearch('')
      } else if (error.response?.status === 401) {
        localStorage.removeItem('accessToken')
        window.location.href = '/'
      } else {
        setWatchlistError(
            error.response?.data?.detail ||
            error.response?.data?.message ||
            'Could not add this stock to your watchlist.'
        )
      }
    }
  }


  const handleCreateAlert = async (event) => {
    event.preventDefault()
    setAlertsError('')

    const form = event.currentTarget
    const formData = new FormData(form)

    const stockId = Number(formData.get('stockId'))
    const targetPrice = Number(formData.get('targetPrice'))
    const condition = formData.get('condition')

    if (!stockId || !Number.isFinite(targetPrice) || targetPrice <= 0) {
      setAlertsError('Please select a stock and enter a target price greater than zero.')
      return
    }

    setCreatingAlert(true)

    try {
      const response = await api.post('/api/alerts', {
        stockId,
        targetPrice,
        condition,
      })

      setAlerts((currentAlerts) => [response.data, ...currentAlerts])
      form.reset()
    } catch (error) {
      if (error.response?.status === 401) {
        localStorage.removeItem('accessToken')
        window.location.href = '/'
        return
      }

      setAlertsError(
          error.response?.data?.detail ||
          error.response?.data?.message ||
          'Could not create the price alert. Please try again.'
      )
    } finally {
      setCreatingAlert(false)
    }
  }


  const handleDeleteAlert = async (alertId) => {
    setAlertsError('')

    try {
      await api.delete(`/api/alerts/${alertId}`)

      setAlerts((currentAlerts) =>
          currentAlerts.filter((alert) => alert.id !== alertId)
      )
    } catch (error) {
      if (error.response?.status === 401) {
        localStorage.removeItem('accessToken')
        window.location.href = '/'
        return
      }

      setAlertsError(
          error.response?.data?.detail ||
          error.response?.data?.message ||
          'Could not delete the alert. Please try again.'
      )
    }
  }



  // Log out and return to the login screen.
  const handleLogout = () => {
    localStorage.removeItem('accessToken')
    window.location.href = '/'
  }

  // Remove a stock from the watchlist.
  const handleRemoveFromWatchlist = async (stockId) => {
    setRemovingStockId(stockId)
    setWatchlistError('')

    try {
      await api.delete(`/api/watchlist/${stockId}`)

      setWatchlist((currentWatchlist) =>
          currentWatchlist.filter((item) => item.stockId !== stockId)
      )

      setWatchlistStockIds((currentIds) =>
          currentIds.filter((id) => id !== stockId)
      )
    } catch (error) {
      if (error.response?.status === 401) {
        localStorage.removeItem('accessToken')
        window.location.href = '/'
        return
      }

      setWatchlistError(
          error.response?.data?.detail ||
          error.response?.data?.message ||
          'Could not remove this stock. Please try again.'
      )
    } finally {
      setRemovingStockId(null)
    }
  }

  if (!token) {
    return <Login />
  }

  const filteredStocks = stocks.filter((stock) =>
      `${stock.symbol ?? ''} ${stock.companyName ?? ''}`
          .toLowerCase()
          .includes(search.toLowerCase())
  )

  const filteredWatchlist = watchlist.filter((stock) =>
      `${stock.symbol ?? ''} ${stock.companyName ?? ''}`
          .toLowerCase()
          .includes(search.toLowerCase())
  )

  const navigation = [
    { name: 'Overview', icon: '◫' },
    { name: 'Stocks', icon: '↗' },
    { name: 'Watchlist', icon: '☆' },
    { name: 'Price alerts', icon: '♧' },
    { name: 'Settings', icon: '⚙' },
  ]

  return (
      <div className="app-shell">
        <aside className="sidebar">
          <div className="brand">
            <div className="brand-icon">S</div>
            <span>
            stock<span className="brand-accent">pulse</span>
          </span>
          </div>

          <p className="nav-label">WORKSPACE</p>

          <nav>
            {navigation.map((item) => (
                <button
                    key={item.name}
                    className={`nav-item ${
                        activePage === item.name ? 'active' : ''
                    }`}
                    onClick={() => {
                      setActivePage(item.name)
                      setSearch('')
                    }}
                >
                  <span className="nav-icon">{item.icon}</span>
                  {item.name}
                </button>
            ))}
          </nav>

          <div className="sidebar-bottom">
            <div className="status-dot" />
            <span>StockPulse workspace</span>
          </div>
        </aside>

        <main className="main-content">
          <header className="topbar">
            <div>
              <p className="eyebrow">MARKET MONITOR</p>
              <h1>{activePage}</h1>
            </div>

            <div className="profile">
              <div className="avatar">SP</div>
              <div>
                <strong>StockPulse User</strong>
                <p>Personal workspace</p>
              </div>
            </div>
          </header>


          {activePage === 'Stock Details' ? (
              <section className="stocks-section stock-details-section">
                <div className="section-heading stock-details-heading">
                  <div>
                    <button
                        type="button"
                        className="back-button"
                        onClick={() => setActivePage('Stocks')}
                    >
                      ← Back to Stocks
                    </button>
                    <h2>{selectedStock?.symbol ?? 'Stock details'}</h2>
                    <p>{selectedStock?.companyName ?? 'Company information'}</p>
                  </div>
                  {selectedStock && (
                      <button
                          type="button"
                          className="nav-item"
                          disabled={watchlistStockIds.includes(selectedStock.id)}
                          onClick={() => handleAddToWatchlist(selectedStock.id)}
                      >
                        {watchlistStockIds.includes(selectedStock.id)
                            ? 'Already in Watchlist'
                            : '+ Add to Watchlist'}
                      </button>
                  )}
                </div>

                <div className="stats-grid stock-detail-stats">
                  <article className="stat-card">
                    <p>Latest recorded price</p>
                    <h3>
                      {selectedStock?.latestPrice != null
                          ? `₹${Number(selectedStock.latestPrice).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                          : '—'}
                    </h3>
                    <span className="muted">Most recent price in StockPulse</span>
                  </article>
                  <article className="stat-card">
                    <p>Exchange</p>
                    <h3>{selectedStock?.exchange ?? '—'}</h3>
                    <span className="muted">Listed exchange</span>
                  </article>
                  <article className="stat-card">
                    <p>Price records</p>
                    <h3>{historyLoading ? '…' : priceHistory.length}</h3>
                    <span className="muted">Historical observations available</span>
                  </article>
                </div>

                <section className="stat-card price-chart-card">
                  <div className="chart-heading">
                    <div>
                      <h3>Historical price</h3>
                      <p>Recorded prices over time</p>
                    </div>
                    <span className="preview-tag">INR (₹)</span>
                  </div>

                  {historyLoading && (
                      <p className="demo-note">Loading price history...</p>
                  )}
                  {!historyLoading && historyError && (
                      <p className="login-error" role="alert">{historyError}</p>
                  )}
                  {!historyLoading && !historyError && priceHistory.length > 0 && (
                      <div className="price-chart">
                        <ResponsiveContainer width="100%" height="100%">
                          <LineChart data={priceHistory} margin={{ top: 12, right: 18, left: 8, bottom: 8 }}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#273249" />
                            <XAxis
                                dataKey="chartTime"
                                tick={{ fill: '#8d9ab4', fontSize: 11 }}
                                tickFormatter={(value) =>
                                    new Date(value).toLocaleString('en-IN', {
                                      day: 'numeric',
                                      month: 'short',
                                      hour: '2-digit',
                                      minute: '2-digit',
                                      hour12: false,
                                    })
                                }
                                minTickGap={24}
                            />
                            <YAxis
                                domain={['auto', 'auto']}
                                tick={{ fill: '#8d9ab4', fontSize: 11 }}
                                tickFormatter={(value) => `₹${Number(value).toLocaleString('en-IN')}`}
                                width={78}
                            />
                            <Tooltip
                                contentStyle={{ background: '#111a2b', border: '1px solid #35415a', borderRadius: 10, color: '#eef3ff' }}
                                labelFormatter={(value) =>
                                    new Date(value).toLocaleString('en-IN', {
                                      day: 'numeric',
                                      month: 'short',
                                      year: 'numeric',
                                      hour: '2-digit',
                                      minute: '2-digit',
                                      hour12: true,
                                    })
                                }
                                formatter={(value) => [`₹${Number(value).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`, 'Price']}
                            />
                            <Line type="monotone" dataKey="price" stroke="#7c9cff" strokeWidth={2.5} dot={{ r: 3, fill: '#7c9cff' }} activeDot={{ r: 5 }} />
                          </LineChart>
                        </ResponsiveContainer>
                      </div>
                  )}
                  {!historyLoading && !historyError && priceHistory.length === 0 && (
                      <div className="empty-state chart-empty-state">No historical price records are available for this stock yet.</div>
                  )}
                </section>

                <section className="stat-card latest-record-card">
                  <h3>Latest price record</h3>
                  <div className="settings-row">
                    <span className="settings-label">Recorded at</span>
                    <strong>{selectedStock?.recordedAt ? new Date(selectedStock.recordedAt).toLocaleString('en-IN') : 'No price record'}</strong>
                  </div>
                  <div className="settings-row">
                    <span className="settings-label">Stock status</span>
                    <span className="preview-tag">{selectedStock?.active ? 'Active' : 'Inactive'}</span>
                  </div>
                </section>
              </section>
          ) : activePage === 'Price alerts' ? (
              <section className="stocks-section">
                <div className="section-heading">
                  <div>
                    <h2>Price alerts</h2>
                    <p>Get notified when a stock reaches your target price.</p>
                  </div>
                </div>

                <form onSubmit={handleCreateAlert} className="stat-card">
                  <h3>Create a price alert</h3>

                  <div className="login-field">
                    <label htmlFor="alert-stock">Select stock</label>
                    <select
                        id="alert-stock"
                        name="stockId"
                        className="search-input"
                        required
                        defaultValue=""
                    >
                      <option value="" disabled>
                        Choose a stock
                      </option>
                      {stocks.map((stock) => (
                          <option key={stock.id} value={stock.id}>
                            {stock.symbol} — {stock.companyName}
                          </option>
                      ))}
                    </select>
                  </div>

                  <div className="login-field">
                    <label htmlFor="alert-target-price">Target price (₹)</label>
                    <input
                        id="alert-target-price"
                        name="targetPrice"
                        className="search-input"
                        type="number"
                        min="0.01"
                        step="0.01"
                        placeholder="e.g. 1500"
                        required
                    />
                  </div>

                  <div className="login-field">
                    <label htmlFor="alert-condition">Alert condition</label>
                    <select
                        id="alert-condition"
                        name="condition"
                        className="search-input"
                        defaultValue="ABOVE"
                        required
                    >
                      <option value="ABOVE">Price goes above target</option>
                      <option value="BELOW">Price goes below target</option>
                    </select>
                  </div>

                  {alertsError && (
                      <p className="login-error" role="alert">
                        {alertsError}
                      </p>
                  )}

                  <button
                      className="login-button"
                      type="submit"
                      disabled={creatingAlert || stocksLoading}
                  >
                    {creatingAlert ? 'Creating alert...' : 'Create alert'}
                  </button>
                </form>

                <div className="section-heading">
                  <div>
                    <h2>Your alerts</h2>
                    <p>Track active and triggered price alerts.</p>
                  </div>
                </div>

                {alertsLoading && (
                    <p className="demo-note">Loading your alerts...</p>
                )}

                {!alertsLoading && alertsError && (
                    <p className="login-error" role="alert">
                      {alertsError}
                    </p>
                )}

                {!alertsLoading && !alertsError && (
                    <div className="table-wrap">
                      <table>
                        <thead>
                        <tr>
                          <th>STOCK</th>
                          <th>TARGET PRICE</th>
                          <th>CONDITION</th>
                          <th>STATUS</th>
                          <th>CREATED AT</th>
                          <th>ACTION</th>
                        </tr>
                        </thead>

                        <tbody>
                        {alerts.map((alert) => (
                            <tr key={alert.id}>
                              <td>
                                <div className="stock-name">
                                  <div className="stock-avatar">
                                    {(alert.symbol ?? '?').slice(0, 1)}
                                  </div>
                                  <div>
                                    <strong>{alert.symbol ?? 'Unknown'}</strong>
                                    <p>{alert.companyName ?? '—'}</p>
                                  </div>
                                </div>
                              </td>

                              <td className="price">
                                ₹{Number(alert.targetPrice).toLocaleString('en-IN', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2,
                              })}
                              </td>

                              <td>{alert.condition}</td>

                              <td>
                                <span className="preview-tag">{alert.status}</span>
                              </td>

                              <td>
                                {alert.createdAt
                                    ? new Date(alert.createdAt).toLocaleString('en-IN')
                                    : '—'}
                              </td>
                              <td>
                                <button
                                    className="nav-item"
                                    onClick={() => handleDeleteAlert(alert.id)}
                                >
                                  Delete
                                </button>
                              </td>
                            </tr>
                        ))}

                        {alerts.length === 0 && (
                            <tr>
                              <td colSpan="6" className="empty-state">
                                No price alerts found. Create your first alert above.
                              </td>
                            </tr>
                        )}
                        </tbody>
                      </table>
                    </div>
                )}
              </section>
          ) : activePage === 'Watchlist' ? (

              <section className="stocks-section">
                <div className="section-heading">
                  <div>
                    <h2>My watchlist</h2>
                    <p>Keep an eye on the stocks that matter to you.</p>
                  </div>

                  <input
                      className="search-input"
                      type="search"
                      placeholder="Search watchlist..."
                      value={search}
                      onChange={(event) => setSearch(event.target.value)}
                  />
                </div>

                <div className="stats-grid">
                  <article className="stat-card">
                    <p>Saved stocks</p>
                    <h3>{watchlistLoading ? '...' : watchlist.length}</h3>
                    <span className="muted">Stocks in your watchlist</span>
                  </article>

                  <article className="stat-card">
                    <p>Prices available</p>
                    <h3>
                      {watchlistLoading
                          ? '...'
                          : watchlist.filter((stock) => stock.latestPrice != null)
                              .length}
                    </h3>
                    <span className="muted">With a recorded price</span>
                  </article>

                  <article className="stat-card">
                    <p>Prices unavailable</p>
                    <h3>
                      {watchlistLoading
                          ? '...'
                          : watchlist.filter((stock) => stock.latestPrice == null)
                              .length}
                    </h3>
                    <span className="muted">No price record found</span>
                  </article>
                </div>

                {watchlistLoading && (
                    <p className="demo-note">Loading your watchlist...</p>
                )}

                {!watchlistLoading && watchlistError && (
                    <p className="login-error" role="alert">
                      {watchlistError}
                    </p>
                )}

                {!watchlistLoading && !watchlistError && (
                    <div className="table-wrap">
                      <table>
                        <thead>
                        <tr>
                          <th>STOCK</th>
                          <th>LATEST PRICE</th>
                          <th>RECORDED AT</th>
                          <th>EXCHANGE</th>
                          <th>ACTION</th>
                        </tr>
                        </thead>

                        <tbody>
                        {filteredWatchlist.map((stock) => (
                            <tr key={stock.id}>
                              <td>
                                <div className="stock-name">
                                  <div className="stock-avatar">
                                    {(stock.symbol ?? '?').slice(0, 1)}
                                  </div>
                                  <div>
                                    <button type="button" className="stock-detail-link" onClick={() => handleOpenStockDetails({ ...stock, id: stock.stockId })}>{stock.symbol ?? 'Unknown'}</button>
                                    <p>{stock.companyName ?? '—'}</p>
                                  </div>
                                </div>
                              </td>

                              <td className="price">
                                {stock.latestPrice != null
                                    ? `₹${Number(stock.latestPrice).toLocaleString(
                                        'en-IN'
                                    )}`
                                    : '—'}
                              </td>

                              <td>
                                {stock.recordedAt
                                    ? new Date(stock.recordedAt).toLocaleString(
                                        'en-IN'
                                    )
                                    : 'No price record'}
                              </td>

                              <td>{stock.exchange ?? '—'}</td>

                              <td>
                                <button
                                    className="nav-item"
                                    disabled={removingStockId === stock.stockId}
                                    onClick={() =>
                                        handleRemoveFromWatchlist(stock.stockId)
                                    }
                                >
                                  {removingStockId === stock.stockId
                                      ? 'Removing...'
                                      : 'Remove'}
                                </button>
                              </td>
                            </tr>
                        ))}

                        {filteredWatchlist.length === 0 && (
                            <tr>
                              <td colSpan="5" className="empty-state">
                                {watchlist.length === 0
                                    ? 'Your watchlist is empty. Add stocks to start tracking them.'
                                    : 'No matching stocks found.'}
                              </td>
                            </tr>
                        )}
                        </tbody>
                      </table>
                    </div>
                )}
              </section>
          ) : activePage === 'Settings' ? (
              <section className="stocks-section">
                <div className="section-heading">
                  <div>
                    <h2>Settings</h2>
                    <p>Manage your StockPulse account and session.</p>
                  </div>
                </div>

                <div className="settings-grid">
                  <section className="settings-card">
                    <h2>Account information</h2>
                    <p>Your current StockPulse workspace profile.</p>
                    <div className="settings-row">
                      <span className="settings-label">Account</span>
                      <strong>StockPulse User</strong>
                    </div>
                    <div className="settings-row">
                      <span className="settings-label">Workspace</span>
                      <strong>Personal workspace</strong>
                    </div>
                  </section>

                  <section className="settings-card">
                    <h2>Session and security</h2>
                    <p>You are signed in using your authenticated session.</p>
                    <div className="settings-row">
                      <span className="settings-label">Session status</span>
                      <span className="preview-tag">Signed in</span>
                    </div>
                    <button
                        type="button"
                        className="logout-button"
                        onClick={handleLogout}
                    >
                      Log out
                    </button>
                  </section>
                </div>
              </section>
          ) : activePage === 'Stocks' ? (
              <section className="stocks-section">
                <div className="section-heading">
                  <div>
                    <h2>All stocks</h2>
                    <p>Browse stocks and add the ones you want to your watchlist.</p>
                  </div>
                  <input
                      className="search-input"
                      type="search"
                      placeholder="Search stocks..."
                      value={search}
                      onChange={(event) => setSearch(event.target.value)}
                  />
                </div>

                {stocksLoading && (
                    <p className="demo-note">Loading stocks and latest prices...</p>
                )}
                {!stocksLoading && stocksError && (
                    <p className="login-error" role="alert">{stocksError}</p>
                )}
                {!stocksLoading && !stocksError && (
                    <div className="table-wrap">
                      <table>
                        <thead>
                        <tr>
                          <th>STOCK</th>
                          <th>LATEST PRICE</th>
                          <th>RECORDED AT</th>
                          <th>STATUS</th>
                          <th>ACTION</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredStocks.map((stock) => (
                            <tr key={stock.id}>
                              <td>
                                <div className="stock-name">
                                  <div className="stock-avatar">{(stock.symbol ?? '?').slice(0, 1)}</div>
                                  <div>
                                    <button type="button" className="stock-detail-link" onClick={() => handleOpenStockDetails(stock)}>{stock.symbol ?? 'Unknown'}</button>
                                    <p>{stock.companyName ?? '—'}</p>
                                  </div>
                                </div>
                              </td>
                              <td className="price">
                                {stock.latestPrice != null
                                    ? `₹${Number(stock.latestPrice).toLocaleString('en-IN')}`
                                    : '—'}
                              </td>
                              <td>
                                {stock.recordedAt
                                    ? new Date(stock.recordedAt).toLocaleString('en-IN')
                                    : 'No price record'}
                              </td>
                              <td><span className="preview-tag">{stock.active ? 'Active' : 'Inactive'}</span></td>
                              <td>
                                <button
                                    type="button"
                                    className="nav-item"
                                    disabled={watchlistStockIds.includes(stock.id)}
                                    onClick={() => handleAddToWatchlist(stock.id)}
                                >
                                  {watchlistStockIds.includes(stock.id) ? 'Already Added' : '+ Add to Watchlist'}
                                </button>
                              </td>
                            </tr>
                        ))}
                        {filteredStocks.length === 0 && (
                            <tr><td colSpan="5" className="empty-state">No matching stocks found.</td></tr>
                        )}
                        </tbody>
                      </table>
                    </div>
                )}
              </section>
          ) : (
              <>
                <section className="welcome">
                  <div>
                    <p className="eyebrow">YOUR MARKET AT A GLANCE</p>
                    <h2>Stay ahead of the market.</h2>
                    <p className="welcome-description">
                      Track stocks, monitor price movements, and manage your alerts.
                    </p>
                  </div>

                  <div className="market-badge">
                    <span className="status-dot" />
                    Latest recorded prices
                  </div>
                </section>

                <section className="stats-grid">
                  <article className="stat-card">
                    <p>Tracked stocks</p>
                    <h3>{stocksLoading ? '...' : stocks.length}</h3>
                    <span className="muted">Stocks returned by the API</span>
                  </article>

                  <article className="stat-card">
                    <p>Stocks with prices</p>
                    <h3>
                      {stocksLoading
                          ? '...'
                          : stocks.filter((stock) => stock.latestPrice != null)
                              .length}
                    </h3>
                    <span className="muted">Latest price available</span>
                  </article>

                  <article className="stat-card">
                    <p>Stocks without prices</p>
                    <h3>
                      {stocksLoading
                          ? '...'
                          : stocks.filter((stock) => stock.latestPrice == null)
                              .length}
                    </h3>
                    <span className="muted">No price record found</span>
                  </article>
                </section>

                <section className="stocks-section">
                  <div className="section-heading">
                    <div>
                      <h2>Market watch</h2>
                      <p>Latest prices recorded by your StockPulse backend</p>
                    </div>

                    <input
                        className="search-input"
                        type="search"
                        placeholder="Search stocks..."
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                    />
                  </div>

                  {stocksLoading && (
                      <p className="demo-note">
                        Loading stocks and latest prices...
                      </p>
                  )}

                  {!stocksLoading && stocksError && (
                      <p className="login-error" role="alert">
                        {stocksError}
                      </p>
                  )}

                  {!stocksLoading && !stocksError && (
                      <div className="table-wrap">
                        <table>
                          <thead>
                          <tr>
                            <th>STOCK</th>
                            <th>LATEST PRICE</th>
                            <th>RECORDED AT</th>
                            <th>STATUS</th>
                            <th>ACTION</th>
                          </tr>
                          </thead>

                          <tbody>
                          {filteredStocks.map((stock) => (
                              <tr key={stock.id}>
                                <td>
                                  <div className="stock-name">
                                    <div className="stock-avatar">
                                      {(stock.symbol ?? '?').slice(0, 1)}
                                    </div>
                                    <div>
                                      <button type="button" className="stock-detail-link" onClick={() => handleOpenStockDetails(stock)}>{stock.symbol ?? 'Unknown'}</button>
                                      <p>{stock.companyName ?? '—'}</p>
                                    </div>
                                  </div>
                                </td>

                                <td className="price">
                                  {stock.latestPrice != null
                                      ? `₹${Number(stock.latestPrice).toLocaleString(
                                          'en-IN'
                                      )}`
                                      : '—'}
                                </td>

                                <td>
                                  {stock.recordedAt
                                      ? new Date(stock.recordedAt).toLocaleString(
                                          'en-IN'
                                      )
                                      : 'No price record'}
                                </td>

                                <td>
                            <span className="preview-tag">
                              {stock.active ? 'Active' : 'Inactive'}
                            </span>
                                </td>

                                <td>
                                  <button
                                      className="nav-item"
                                      disabled={watchlistStockIds.includes(stock.id)}
                                      onClick={() => handleAddToWatchlist(stock.id)}
                                  >
                                    {watchlistStockIds.includes(stock.id)
                                        ? 'Already Added'
                                        : '+ Add to Watchlist'}
                                  </button>
                                </td>
                              </tr>
                          ))}

                          {filteredStocks.length === 0 && (
                              <tr>
                                <td colSpan="5" className="empty-state">
                                  No matching stocks found.
                                </td>
                              </tr>
                          )}
                          </tbody>
                        </table>
                      </div>
                  )}
                </section>
              </>
          )}
        </main>
      </div>
  )
}

export default App
