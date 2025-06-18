<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Fcm_model extends CI_Model {

    public function __construct() {
        parent::__construct();
        $this->load->database();
    }

    /**
     * Register or update FCM token for customer
     */
    public function register_token($customer_id, $fcm_token) {
        $data = [
            'customer_id' => $customer_id,
            'fcm_token' => $fcm_token,
            'status' => 'pending'
        ];

        // Check if customer already exists
        $existing = $this->db->get_where('customer_tokens', ['customer_id' => $customer_id])->row_array();
        
        if ($existing) {
            // Update existing record
            $this->db->where('customer_id', $customer_id);
            return $this->db->update('customer_tokens', [
                'fcm_token' => $fcm_token,
                'updated_at' => date('Y-m-d H:i:s')
            ]);
        } else {
            // Insert new record
            return $this->db->insert('customer_tokens', $data);
        }
    }

    /**
     * Get customer FCM token and data
     */
    public function get_customer_token($customer_id) {
        return $this->db->get_where('customer_tokens', ['customer_id' => $customer_id])->row_array();
    }

    /**
     * Update delivery status
     */
    public function update_delivery_status($customer_id, $status) {
        $this->db->where('customer_id', $customer_id);
        return $this->db->update('customer_tokens', [
            'status' => $status,
            'updated_at' => date('Y-m-d H:i:s')
        ]);
    }

    /**
     * Get all customers with their status
     */
    public function get_all_customers() {
        $this->db->select('customer_id, status, created_at, updated_at');
        $this->db->order_by('updated_at', 'DESC');
        return $this->db->get('customer_tokens')->result_array();
    }

    /**
     * Get customers by status
     */
    public function get_customers_by_status($status) {
        return $this->db->get_where('customer_tokens', ['status' => $status])->result_array();
    }
}