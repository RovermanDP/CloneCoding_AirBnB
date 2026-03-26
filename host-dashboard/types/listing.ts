export type ListingStatus = "Published" | "Draft";

export type Listing = {
  id: number;
  name: string;
  price: string;
  location: string;
  status: ListingStatus;
  updatedAt: string;
};

export type ListingListResponse = {
  listings: Listing[];
};

export type UpdateListingStatusPayload = {
  status: ListingStatus;
};

export type UpdateListingStatusResponse = {
  listing: Listing;
};
