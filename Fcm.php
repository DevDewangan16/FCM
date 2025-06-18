<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Fcm extends CI_Controller {

    public function __construct() {
        parent::__construct();
        $this->load->model('Fcm_model');
        $this->load->library('form_validation');
        header('Content-Type: application/json');
    }

    /**
     * Register FCM token for customer
     * POST /fcm/register
     */
    public function register() {
        if ($this->input->method() !== 'post') {
            $this->output
                ->set_status_header(405)
                ->set_output(json_encode(['error' => 'Method not allowed']));
            return;
        }

        $input = json_decode($this->input->raw_input_stream, true);
        
        if (empty($input['customer_id']) || empty($input['fcm_token'])) {
            $this->output
                ->set_status_header(400)
                ->set_output(json_encode(['error' => 'customer_id and fcm_token are required']));
            return;
        }

        $result = $this->Fcm_model->register_token($input['customer_id'], $input['fcm_token']);
        
        if ($result) {
            $this->output
                ->set_status_header(200)
                ->set_output(json_encode(['message' => 'Token registered successfully']));
        } else {
            $this->output
                ->set_status_header(500)
                ->set_output(json_encode(['error' => 'Failed to register token']));
        }
    }

    /**
     * Mark delivery as completed and send notification
     * POST /fcm/delivery
     */
    public function delivery() {
        if ($this->input->method() !== 'post') {
            $this->output
                ->set_status_header(405)
                ->set_output(json_encode(['error' => 'Method not allowed']));
            return;
        }

        $input = json_decode($this->input->raw_input_stream, true);
        
        if (empty($input['customer_id'])) {
            $this->output
                ->set_status_header(400)
                ->set_output(json_encode(['error' => 'customer_id is required']));
            return;
        }

        // Get customer token
        $customer_data = $this->Fcm_model->get_customer_token($input['customer_id']);
        
        if (!$customer_data) {
            $this->output
                ->set_status_header(404)
                ->set_output(json_encode(['error' => 'Customer not found']));
            return;
        }

        // Update delivery status
        $update_result = $this->Fcm_model->update_delivery_status($input['customer_id'], 'delivered');
        
        if (!$update_result) {
            $this->output
                ->set_status_header(500)
                ->set_output(json_encode(['error' => 'Failed to update delivery status']));
            return;
        }

        // Send FCM notification
        $notification_result = $this->send_fcm_notification(
            $customer_data['fcm_token'],
            'Delivery Update',
            'Your order has been delivered successfully!'
        );

        if ($notification_result['success']) {
            $this->output
                ->set_status_header(200)
                ->set_output(json_encode([
                    'message' => 'Delivery status updated and notification sent',
                    'fcm_response' => $notification_result['response']
                ]));
        } else {
            $this->output
                ->set_status_header(207)
                ->set_output(json_encode([
                    'message' => 'Delivery status updated but notification failed',
                    'error' => $notification_result['error']
                ]));
        }
    }

    /**
     * Get all customers with their status
     * GET /fcm/customers
     */
    public function customers() {
        if ($this->input->method() !== 'get') {
            $this->output
                ->set_status_header(405)
                ->set_output(json_encode(['error' => 'Method not allowed']));
            return;
        }

        $customers = $this->Fcm_model->get_all_customers();
        
        $this->output
            ->set_status_header(200)
            ->set_output(json_encode(['customers' => $customers]));
    }

    /**
     * Send FCM notification using HTTP v1 API
     */
    private function send_fcm_notification($fcm_token, $title, $body) {
        // Load FCM service account key (you need to add this file)
        $service_account_path = APPPATH . 'config/firebase-service-account.json';
        
        if (!file_exists($service_account_path)) {
            return ['success' => false, 'error' => 'Firebase service account file not found'];
        }

        $service_account = json_decode(file_get_contents($service_account_path), true);
        
        // Get access token
        $access_token = $this->get_access_token($service_account);
        
        if (!$access_token) {
            return ['success' => false, 'error' => 'Failed to get access token'];
        }

        // Prepare FCM message
        $fcm_message = [
            'message' => [
                'token' => $fcm_token,
                'notification' => [
                    'title' => $title,
                    'body' => $body
                ]
            ]
        ];

        // Send notification
        $project_id = $service_account['project_id'];
        $url = "https://fcm.googleapis.com/v1/projects/{$project_id}/messages:send";
        
        $headers = [
            'Authorization: Bearer ' . $access_token,
            'Content-Type: application/json'
        ];

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fcm_message));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

        $response = curl_exec($ch);
        $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($http_code === 200) {
            return ['success' => true, 'response' => json_decode($response, true)];
        } else {
            return ['success' => false, 'error' => $response];
        }
    }

    /**
     * Get OAuth2 access token for FCM
     */
    private function get_access_token($service_account) {
        $private_key = $service_account['private_key'];
        $client_email = $service_account['client_email'];
        
        // Create JWT header
        $header = json_encode(['typ' => 'JWT', 'alg' => 'RS256']);
        
        // Create JWT payload
        $now = time();
        $payload = json_encode([
            'iss' => $client_email,
            'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
            'aud' => 'https://oauth2.googleapis.com/token',
            'exp' => $now + 3600,
            'iat' => $now
        ]);
        
        // Encode header and payload
        $header_encoded = rtrim(strtr(base64_encode($header), '+/', '-_'), '=');
        $payload_encoded = rtrim(strtr(base64_encode($payload), '+/', '-_'), '=');
        
        // Create signature
        $signature_input = $header_encoded . '.' . $payload_encoded;
        openssl_sign($signature_input, $signature, $private_key, OPENSSL_ALGO_SHA256);
        $signature_encoded = rtrim(strtr(base64_encode($signature), '+/', '-_'), '=');
        
        // Create JWT
        $jwt = $header_encoded . '.' . $payload_encoded . '.' . $signature_encoded;
        
        // Exchange JWT for access token
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, 'https://oauth2.googleapis.com/token');
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' => $jwt
        ]));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        
        $response = curl_exec($ch);
        curl_close($ch);
        
        $token_data = json_decode($response, true);
        
        return isset($token_data['access_token']) ? $token_data['access_token'] : null;
    }
}