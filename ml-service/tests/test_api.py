import json
import unittest
import sys
import os

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from app import app

class TestMlApi(unittest.TestCase):
    def setUp(self):
        app.config['TESTING'] = True
        self.client = app.test_client()

    def test_health(self):
        rv = self.client.get('/health')
        self.assertEqual(rv.status_code, 200)
        json_data = rv.get_json()
        self.assertEqual(json_data['status'], 'UP')

    def test_model_info(self):
        rv = self.client.get('/model/info')
        self.assertEqual(rv.status_code, 200)
        json_data = rv.get_json()
        self.assertIn('model_version', json_data)
        self.assertEqual(json_data['model_version'], 'v2.0')

    def test_metrics(self):
        rv = self.client.get('/metrics')
        self.assertEqual(rv.status_code, 200)
        json_data = rv.get_json()
        self.assertIn('total_predictions', json_data)

    def test_predict_attrition_invalid_age(self):
        payload = {"employee_id": "TEST-ERR", "age": 150, "monthly_income": 4500.0}
        rv = self.client.post('/predict', data=json.dumps(payload), content_type='application/json')
        self.assertEqual(rv.status_code, 400)
        json_data = rv.get_json()
        self.assertIn("Âge invalide", json_data['error'])

    def test_predict_anomaly_invalid_amount(self):
        payload = {"amount": -500.0, "category": "MEAL"}
        rv = self.client.post('/predict/anomaly', data=json.dumps(payload), content_type='application/json')
        self.assertEqual(rv.status_code, 400)
        json_data = rv.get_json()
        self.assertIn("Montant de dépense hors limites", json_data['error'])

    def test_predict_attrition(self):
        payload = {
            "employee_id": "TEST-100",
            "age": 32,
            "monthly_income": 4500.0,
            "years_at_company": 3,
            "job_satisfaction": 1,
            "work_life_balance": 1,
            "overtime": 1,
            "years_since_last_promotion": 3,
            "distance_from_home": 15,
            "environment_satisfaction": 2,
            "job_level": 2,
            "num_companies_worked": 2,
            "stock_option_level": 0,
            "training_times_last_year": 2,
            "total_working_years": 8,
            "years_in_current_role": 3
        }
        rv = self.client.post('/predict', data=json.dumps(payload), content_type='application/json')
        self.assertEqual(rv.status_code, 200)
        json_data = rv.get_json()
        self.assertIn('leave_probability', json_data)
        self.assertIn('risk_level', json_data)

    def test_predict_anomaly_normal(self):
        payload = {
            "amount": 150.0,
            "category": "MEAL",
            "job_level": "Senior",
            "day_of_week": "MONDAY",
            "submission_hour": 13,
            "travel_distance_km": 0.0,
            "receipt_uploaded": 1,
            "claim_delay_days": 2
        }
        rv = self.client.post('/predict/anomaly', data=json.dumps(payload), content_type='application/json')
        self.assertEqual(rv.status_code, 200)
        json_data = rv.get_json()
        self.assertFalse(json_data['is_anomaly'])
        self.assertEqual(json_data['risk_level'], 'NORMAL')

    def test_predict_anomaly_suspect(self):
        payload = {
            "amount": 5400.0,
            "category": "HOTEL",
            "job_level": "Junior",
            "day_of_week": "SUNDAY",
            "submission_hour": 23,
            "travel_distance_km": 25.0,
            "receipt_uploaded": 0,
            "claim_delay_days": 25
        }
        rv = self.client.post('/predict/anomaly', data=json.dumps(payload), content_type='application/json')
        self.assertEqual(rv.status_code, 200)
        json_data = rv.get_json()
        self.assertTrue(json_data['is_anomaly'])
        self.assertEqual(json_data['risk_level'], 'SUSPECT')

if __name__ == '__main__':
    unittest.main()
