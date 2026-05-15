
interface ApiResponse<T> {
    status: number;
    message: string;
    data: T;
    timestamp: string;
}

interface PaginatedResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}
