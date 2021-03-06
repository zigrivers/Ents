#include <AnyMemory.h>
#include <iomanip>

using namespace std;

AnyMemory::AnyMemory(const AnyMemory &copy)
{
  set(copy);
}

AnyMemory::AnyMemory()
{
}

AnyMemory::~AnyMemory() 
{
  if (data != nullptr) {
    free(data);
    data = nullptr;
  }
}

void AnyMemory::set(const AnyMemory &copy)
{
  setSize(copy.size);
  memcpy(data, copy.data, copy.size); 
}

AnyMemory AnyMemory::sub(const size_t offset, const size_t bytes)
{
  AnyMemory am;
  am.setSize(bytes);
  memcpy(am.data, data + offset, bytes);
  return am;
}

size_t AnyMemory::append(const AnyMemory &copy) 
{
  const size_t offset = size;

  setSize(size + copy.size);
  memcpy(data + offset, copy.data, copy.size);

  return offset;
}

bool AnyMemory::equals(const AnyMemory &other) const
{
  return ( size == other.size && memcmp(data, other.data, size) == 0 );
}

int AnyMemory::hashCode() const
{
  const int prime = 31;
  int hash = 0;
  for (size_t i = 0; i < size; i++) {
    hash = hash * prime + (*((char*)(data + i)));
  }
  return hash;
}

int AnyMemory::compareTo(const AnyMemory &other) const
{
  int d = (size - other.size);

  return ( d != 0 ? d : memcmp( data, other.data, size ) );
}

ostream& operator<<(ostream &out, AnyMemory const &a)
{
  #define HEX(d,x) setw(d) << setfill('0') << hex << uppercase << (long long)(x)

  out << "[AnyMemory]{size:" << a.size << ", ptr:0x" << HEX(sizeof(void*), a.data) << ", data:0x";

  for (size_t i = 0; i < a.size; i++) {
    out << HEX(2, a.get<char>(i));
  }

  out << "}";

  return out;
}