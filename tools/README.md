# LCL Measurement Tool Test Server

This is a Flask-based test server that replaces the Seattle Community Network API for the LCL Measurement Tool Android app.

## Prerequisites

- Python 3.6 or higher
- pip (Python package installer)

## Installation

1. Install the required Python packages:

```bash
pip install flask flask-cors
```

## Running the Server

1. Navigate to the tools directory:

```bash
cd tools
```

2. Run the server:

```bash
python test_server.py
```

The server will start on `http://localhost:5000` and listen on all interfaces.

## API Endpoints

- `POST /api/register` - Device registration
- `POST /api/report_signal` - Signal strength reports
- `POST /api/report_measurement` - Connectivity reports
- `GET /api/stats` - Server statistics
- `GET /api/health` - Health check

## Database

The server uses SQLite database (`lcl_measurements.db`) which is created automatically on first run.

## Notes

- The server runs in debug mode by default
- CORS is enabled for all routes
- All data is stored locally in the SQLite database
- **Important**: In `NetworkConstants.kt`, replace the URL with `http://198.....:5000` (the server's IP address and port that it outputs when starting)