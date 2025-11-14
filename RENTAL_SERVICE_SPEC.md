1. Overview
The rental-service handles all operations related to house and apartment rentals.
It allows users to post rental listings, search for available properties, send rental requests, and leave reviews.
This service communicates with:
auth-service → for authentication (JWT)
user-service → for retrieving user details (owner/renter info)
2. Modules
2.1 Rental Listing Module
Manages rental listings posted by users (owners/lessors).
Features:
Create, update, delete rental posts
Attach photos, descriptions, and pricing info
Set status (available / rented / inactive)
Retrieve listings by user
2.2 Rental Request Module
Allows renters to send rental requests to lessors and track their status.
Features:
Send a rental request for a listing
View request details
Update status (pending, accepted, rejected, canceled)
Retrieve all requests made by a user
2.3 Search & Filter Module
Provides APIs to search and filter listings.
Features:
Search by keyword, price range, location, property type
Sort by newest, lowest price, highest rating
Pagination support
2.4 Review & Rating Module
Allows renters to leave reviews and ratings for properties and owners.
Features:
Add review after rental completion
Edit or delete own review
Retrieve reviews for a given rental
2.5 Admin Module
Admin-only operations to moderate and manage the rental marketplace.
Features:
View all rental listings
Change status (approve, reject, deactivate)
View reported listings
3. API Endpoints
3.1 Rental Listing Endpoints
Endpoint	Method	Description
/api/rentals	POST	Create a new rental listing
/api/rentals/{id}	GET	Get rental listing details
/api/rentals/{id}	PUT	Update a rental listing
/api/rentals/{id}	DELETE	Delete a rental listing
/api/rentals/user/{userId}	GET	Get all listings posted by a user
3.2 Rental Request Endpoints
Endpoint	Method	Description
/api/rentals/{rentalId}/requests	POST	Send a rental request
/api/rentals/requests/{requestId}	GET	Get rental request details
/api/rentals/requests/{requestId}/status	PATCH	Update rental request status
/api/rentals/user/{userId}/requests	GET	List all requests by a user
3.3 Search & Filter Endpoints
Endpoint	Method	Description
/api/rentals/search	GET	Search rentals with filters (price, location, type, etc.)
Query Parameters Example:
/api/rentals/search?city=Taipei&minPrice=10000&maxPrice=30000&type=apartment&page=0&size=10&sort=price,asc
3.4 Review & Rating Endpoints
Endpoint	Method	Description
/api/rentals/{rentalId}/reviews	POST	Add a review for a rental
/api/rentals/{rentalId}/reviews	GET	Get all reviews for a rental
/api/reviews/{reviewId}	DELETE	Delete a review
3.5 Admin Endpoints
Endpoint	Method	Description
/api/admin/rentals	GET	Get all rental listings
/api/admin/rentals/{id}/status	PATCH	Change rental listing status (approve/reject/deactivate)
/api/admin/reports	GET	View reported listings
4. Future Integration
Integration with payment-service for handling transactions.
Integration with recommendation-service for personalized rental suggestions.
Support for advanced analytics and fraud detection.