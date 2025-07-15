# ByteBridge

File Storage System analogous to Dropbox

## Why SQL DB:
1. Data (including File metadata) is structured with clear relationships
2. ACID Compliance Critical
   1. File uploads: Need atomicity (either file saves completely or not at all)
   2. User registration: Must ensure email uniqueness 
   3. File sharing: Consistent permission management 
   4. Storage quotas: Accurate file size tracking
