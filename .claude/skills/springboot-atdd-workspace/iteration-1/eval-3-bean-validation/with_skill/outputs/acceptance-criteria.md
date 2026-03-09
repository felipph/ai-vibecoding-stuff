# Order Entity Bean Validation - Acceptance Criteria

## Business Requirements

### Order Items Validation
[+] Order can be created with one or more items
[-] Order with empty items list is rejected with message "Order items cannot be empty"
[-] Order with null items list is rejected with message "Order items cannot be empty"

### Total Amount Validation
[+] Order can be created with positive total amount
[-] Order with zero total amount is rejected with message "Total amount must be positive"
[-] Order with negative total amount is rejected with message "Total amount must be positive"
[-] Order with null total amount is rejected with message "Total amount is required"

### Customer Email Validation
[+] Order can be created with valid email address
[-] Order with null email is rejected with message "Customer email is required"
[-] Order with empty email is rejected with message "Customer email is required"
[-] Order with invalid email format is rejected with message "Customer email must be valid"

### Order Date Validation
[+] Order can be created with current date
[+] Order can be created with past date
[-] Order with future date is rejected with message "Order date cannot be in the future"
[-] Order with null date is rejected with message "Order date is required"
