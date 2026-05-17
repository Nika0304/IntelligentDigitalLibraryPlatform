import axios from "axios";

const BASE = process.env.REACT_APP_BACKEND_URL;
export const api = axios.create({ baseURL: `${BASE}/api`, headers: { "Content-Type": "application/json" } });

// Books
export const fetchBooks = () => api.get("/books").then((r) => r.data);
export const fetchBook = (id) => api.get(`/books/${id}`).then((r) => r.data);
export const searchBooks = (q) => api.get(`/books/search`, { params: { title: q } }).then((r) => r.data);
export const fetchBooksByCategory = (cid) => api.get(`/books/category/${cid}`).then((r) => r.data);
export const fetchDigitalBooks = () => api.get(`/books/digital`).then((r) => r.data);
export const fetchPhysicalBooks = () => api.get(`/books/physical`).then((r) => r.data);
export const createBook = (b) => api.post(`/books`, b).then((r) => r.data);
export const updateBook = (id, b) => api.put(`/books/${id}`, b).then((r) => r.data);
export const deleteBook = (id) => api.delete(`/books/${id}`);

// Categories & Authors
export const fetchCategories = () => api.get("/categories").then((r) => r.data);
export const createCategory = (name) => api.post("/categories", { name }).then((r) => r.data);
export const deleteCategory = (id) => api.delete(`/categories/${id}`);

export const fetchAuthors = () => api.get("/authors").then((r) => r.data);
export const createAuthor = (name) => api.post("/authors", { name }).then((r) => r.data);
export const deleteAuthor = (id) => api.delete(`/authors/${id}`);

// Users / Auth
export const login = (email, password) => api.post("/auth/login", { email, password }).then((r) => r.data);
export const register = (data) => api.post("/users", { ...data, roleId: 1 }).then((r) => r.data);
export const fetchUsers = () => api.get("/users").then((r) => r.data);
export const updateUserStatus = (uid, status) =>
  api.put(`/users/${uid}/status`, { status }).then((r) => r.data);
export const deleteUser = (uid) => api.delete(`/users/${uid}`);

// Reservations
export const createReservation = (userId, bookId) =>
  api.post(`/reservations`, { userId, bookId }).then((r) => r.data);
export const fetchUserReservations = (uid) => api.get(`/reservations/user/${uid}`).then((r) => r.data);
export const fetchAllReservations = () => api.get(`/reservations`).then((r) => r.data);
export const cancelReservation = (rid) => api.put(`/reservations/${rid}/cancel`).then((r) => r.data);
export const markReadyForPickup = (rid) => api.put(`/reservations/${rid}/ready-for-pickup`).then((r) => r.data);
export const borrowBook = (rid) => api.put(`/borrowings/${rid}/borrow`).then((r) => r.data);
export const returnBook = (rid) => api.put(`/borrowings/${rid}/return`).then((r) => r.data);

// Book copies
export const fetchCopiesByBook = (bid) => api.get(`/book-copies/book/${bid}`).then((r) => r.data);
export const createBookCopy = (inventoryCode, bookId) =>
  api.post(`/book-copies`, { inventoryCode, bookId }).then((r) => r.data);
export const updateCopyStatus = (cid, status) =>
  api.put(`/book-copies/${cid}/status`, { status }).then((r) => r.data);
export const deleteCopy = (cid) => api.delete(`/book-copies/${cid}`);

// Reviews
export const fetchReviewsByBook = (bid) => api.get(`/reviews/book/${bid}`).then((r) => r.data);
export const createReview = (data) => api.post(`/reviews`, data).then((r) => r.data);
export const deleteReview = (rid) => api.delete(`/reviews/${rid}`);

// Notifications
export const fetchUserNotifications = (uid) => api.get(`/notifications/user/${uid}`).then((r) => r.data);
export const markNotificationRead = (nid) => api.put(`/notifications/${nid}/read`).then((r) => r.data);
export const markAllRead = (uid) => api.put(`/notifications/user/${uid}/read-all`);

// Wishlist / Downloads
export const fetchWishlist = (uid) => api.get(`/wishlist/user/${uid}`).then((r) => r.data);
export const addToWishlist = (userId, bookId) =>
  api.post(`/wishlist`, { userId, bookId }).then((r) => r.data);
export const removeFromWishlist = (userId, bookId) =>
  api.delete(`/wishlist`, { params: { userId, bookId } });
export const fetchDownloads = (uid) => api.get(`/downloads/user/${uid}`).then((r) => r.data);
export const recordDownload = (userId, bookId) =>
  api.post(`/downloads`, { userId, bookId }).then((r) => r.data);

// Stats
export const fetchStats = () => api.get(`/stats`).then((r) => r.data);


export const searchGoogleBooks = (q) =>
    api.get("/external/books", { params: { q } }).then((r) =>
        r.data.map((book) => ({
            ...book,
            googleId: book.externalId,
        }))
    );

export const fetchAllFines = () =>
    api.get("/fines").then((r) => r.data);

export const payFine = (fineId) =>
    api.put(`/fines/${fineId}/pay`).then((r) => r.data);

export const cancelFine = (fineId) =>
    api.put(`/fines/${fineId}/cancel`).then((r) => r.data);