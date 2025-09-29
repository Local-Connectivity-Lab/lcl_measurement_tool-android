#!/usr/bin/env python3
"""
LCL Measurement Tool Server
A Flask server to replace the Seattle Community Network API
for the LCL Measurement Tool Android app.
"""

from flask import Flask, request, jsonify, make_response
from flask_cors import CORS
import json
import sqlite3
import logging
from datetime import datetime
import os
from typing import Dict, Any, List

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Database setup
DATABASE_PATH = 'lcl_measurements.db'

def init_database():
    """Initialize SQLite database with required tables"""
    conn = sqlite3.connect(DATABASE_PATH)
    cursor = conn.cursor()
    
    # Registration table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS registrations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            sigma_r TEXT NOT NULL,
            h TEXT NOT NULL,
            R TEXT NOT NULL,
            registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    
    # Signal strength reports table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS signal_reports (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            latitude REAL NOT NULL,
            longitude REAL NOT NULL,
            timestamp TEXT NOT NULL,
            cell_id TEXT NOT NULL,
            device_id TEXT NOT NULL,
            dbm INTEGER NOT NULL,
            level_code INTEGER NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    
    # Connectivity reports table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS connectivity_reports (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            latitude REAL NOT NULL,
            longitude REAL NOT NULL,
            timestamp TEXT NOT NULL,
            cell_id TEXT NOT NULL,
            device_id TEXT NOT NULL,
            upload_speed REAL NOT NULL,
            download_speed REAL NOT NULL,
            ping REAL NOT NULL,
            package_loss REAL NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    
    conn.commit()
    conn.close()
    logger.info("Database initialized successfully")

def get_db_connection():
    """Get database connection"""
    conn = sqlite3.connect(DATABASE_PATH)
    conn.row_factory = sqlite3.Row
    return conn

@app.route('/api/register', methods=['POST'])
def register():
    """
    Handle device registration
    Expected JSON: {"sigma_r": "value", "h": "value", "R": "value"}
    """
    try:
        # Parse JSON data
        data = request.get_json()
        if not data:
            return make_response(jsonify({"error": "No JSON data provided"}), 400)
        
        # Validate required fields
        required_fields = ['sigma_r', 'h', 'R']
        for field in required_fields:
            if field not in data:
                return make_response(jsonify({"error": f"Missing required field: {field}"}), 400)
        
        # Store registration in database
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('''
            INSERT INTO registrations (sigma_r, h, R)
            VALUES (?, ?, ?)
        ''', (data['sigma_r'], data['h'], data['R']))
        
        registration_id = cursor.lastrowid
        conn.commit()
        conn.close()
        
        logger.info(f"Device registered successfully with ID: {registration_id}")
        
        # Return success response
        response_data = {
            "status": "success",
            "message": "Device registered successfully",
            "registration_id": registration_id
        }
        
        return make_response(jsonify(response_data), 200)
        
    except json.JSONDecodeError:
        return make_response(jsonify({"error": "Invalid JSON format"}), 400)
    except Exception as e:
        logger.error(f"Registration error: {str(e)}")
        return make_response(jsonify({"error": "Internal server error"}), 500)

@app.route('/api/report_signal', methods=['POST'])
def report_signal():
    """
    Handle signal strength reports
    Expected JSON: {
        "latitude": float,
        "longitude": float,
        "timestamp": "string",
        "cell_id": "string",
        "device_id": "string",
        "dbm": int,
        "level_code": int
    }
    """
    try:
        # Parse JSON data
        data = request.get_json()
        if not data:
            return make_response(jsonify({"error": "No JSON data provided"}), 400)
        
        # Validate required fields
        required_fields = ['latitude', 'longitude', 'timestamp', 'cell_id', 'device_id', 'dbm', 'level_code']
        for field in required_fields:
            if field not in data:
                return make_response(jsonify({"error": f"Missing required field: {field}"}), 400)
        
        # Store signal report in database
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('''
            INSERT INTO signal_reports (latitude, longitude, timestamp, cell_id, device_id, dbm, level_code)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        ''', (
            data['latitude'], data['longitude'], data['timestamp'],
            data['cell_id'], data['device_id'], data['dbm'], data['level_code']
        ))
        
        report_id = cursor.lastrowid
        conn.commit()
        conn.close()
        
        logger.info(f"Signal report stored successfully with ID: {report_id}")
        
        # Return success response
        response_data = {
            "status": "success",
            "message": "Signal report received successfully",
            "report_id": report_id
        }
        
        return make_response(jsonify(response_data), 200)
        
    except json.JSONDecodeError:
        return make_response(jsonify({"error": "Invalid JSON format"}), 400)
    except Exception as e:
        logger.error(f"Signal report error: {str(e)}")
        return make_response(jsonify({"error": "Internal server error"}), 500)

@app.route('/api/report_measurement', methods=['POST'])
def report_measurement():
    """
    Handle connectivity measurement reports
    Expected JSON: {
        "latitude": float,
        "longitude": float,
        "timestamp": "string",
        "cell_id": "string",
        "device_id": "string",
        "upload_speed": float,
        "download_speed": float,
        "ping": float,
        "package_loss": float
    }
    """
    try:
        # Parse JSON data
        data = request.get_json()
        if not data:
            return make_response(jsonify({"error": "No JSON data provided"}), 400)
        
        # Validate required fields
        required_fields = ['latitude', 'longitude', 'timestamp', 'cell_id', 'device_id', 
                          'upload_speed', 'download_speed', 'ping', 'package_loss']
        for field in required_fields:
            if field not in data:
                return make_response(jsonify({"error": f"Missing required field: {field}"}), 400)
        
        # Store connectivity report in database
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('''
            INSERT INTO connectivity_reports 
            (latitude, longitude, timestamp, cell_id, device_id, upload_speed, download_speed, ping, package_loss)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            data['latitude'], data['longitude'], data['timestamp'],
            data['cell_id'], data['device_id'], data['upload_speed'],
            data['download_speed'], data['ping'], data['package_loss']
        ))
        
        report_id = cursor.lastrowid
        conn.commit()
        conn.close()
        
        logger.info(f"Connectivity report stored successfully with ID: {report_id}")
        
        # Return success response
        response_data = {
            "status": "success",
            "message": "Connectivity report received successfully",
            "report_id": report_id
        }
        
        return make_response(jsonify(response_data), 200)
        
    except json.JSONDecodeError:
        return make_response(jsonify({"error": "Invalid JSON format"}), 400)
    except Exception as e:
        logger.error(f"Connectivity report error: {str(e)}")
        return make_response(jsonify({"error": "Internal server error"}), 500)

# Additional endpoints for data visualization/management
@app.route('/api/stats', methods=['GET'])
def get_stats():
    """Get basic statistics about collected data"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # Get counts
        cursor.execute('SELECT COUNT(*) FROM registrations')
        registration_count = cursor.fetchone()[0]
        
        cursor.execute('SELECT COUNT(*) FROM signal_reports')
        signal_count = cursor.fetchone()[0]
        
        cursor.execute('SELECT COUNT(*) FROM connectivity_reports')
        connectivity_count = cursor.fetchone()[0]
        
        conn.close()
        
        stats = {
            "registrations": registration_count,
            "signal_reports": signal_count,
            "connectivity_reports": connectivity_count,
            "server_status": "running"
        }
        
        return jsonify(stats)
        
    except Exception as e:
        logger.error(f"Stats error: {str(e)}")
        return make_response(jsonify({"error": "Internal server error"}), 500)

@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "version": "1.0.0"
    })

@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({"error": "Endpoint not found"}), 404)

@app.errorhandler(405)
def method_not_allowed(error):
    return make_response(jsonify({"error": "Method not allowed"}), 405)

if __name__ == '__main__':
    # Initialize database
    init_database()
    
    # Run the server
    print("Starting LCL Measurement Tool Server...")
    print("Server will be available at: http://localhost:5000")
    print("API Base URL: http://localhost:5000/api/")
    print("\nEndpoints:")
    print("  POST /api/register - Device registration")
    print("  POST /api/report_signal - Signal strength reports")
    print("  POST /api/report_measurement - Connectivity reports")
    print("  GET  /api/stats - Server statistics")
    print("  GET  /api/health - Health check")
    
    app.run(
        host='0.0.0.0',  # Listen on all interfaces
        port=5000,
        debug=True,
        threaded=True
    )